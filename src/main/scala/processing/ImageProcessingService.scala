package org.bpc
package processing

import Util.BufferedImageExtension

import org.jocl.CL._
import org.jocl._

import java.awt.image.BufferedImage
import java.util.concurrent.LinkedBlockingQueue
import scala.util.Try

class ImageProcessingService {
    CL.setExceptionsEnabled(true)

    // print opencl version
    val platformVersion = {
        val array = new Array[Byte](1024)
        clGetPlatformInfo(platform, CL_PLATFORM_VERSION, array.length, Pointer.to(array), null)
        new String(array).trim
    }

    println(s"OpenCL Version: $platformVersion")

    lazy val context: cl_context = {
        val properties = new cl_context_properties()
        properties.addProperty(CL_CONTEXT_PLATFORM, platform)
        val array = Array.fill[cl_device_id](1)(device)
        clCreateContext(properties, 1, array, null, null, null)
    }

    lazy val queue: cl_command_queue = {
        val properties = new cl_queue_properties()
        clCreateCommandQueueWithProperties(context, device, properties, null)
    }

    lazy private val platform: cl_platform_id = {
        val num_platforms = {
            val array = new Array[Int](1)
            clGetPlatformIDs(0, null, array)
            array.head
        }
        val platforms = new Array[cl_platform_id](num_platforms)
        clGetPlatformIDs(platforms.length, platforms, null)
        platforms.head
    }

    lazy private val device: cl_device_id = {
        val num_devices = {
            val array = new Array[Int](1)
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, array)
            array.head
        }
        val devices = new Array[cl_device_id](num_devices)
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, num_devices, devices, null)
        devices.head
    }

    private val kernelQueue = new LinkedBlockingQueue[ImageKernel]()
    def close(): Unit = {
        clReleaseCommandQueue(queue)
        clReleaseContext(context)
    }

    def enqueue(kernel: ImageKernel): Unit = {
        kernelQueue.put(kernel)
    }

    def execute(image: BufferedImage): Unit = Try {
        val imageFormat = new cl_image_format()
        imageFormat.image_channel_data_type = CL_UNSIGNED_INT8
        imageFormat.image_channel_order = CL_RGBA

        // while the queue is not empty, execute the next kernel, then swap the buffers, then execute the next kernel, etc.
        var inputBuffer = clCreateImage2D(context, CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR, Array.fill(1)(imageFormat), image.getWidth, image.getHeight, image.getWidth * Sizeof.cl_uint, image.pointer(), null)
        var outputBuffer = clCreateImage2D(context, CL_MEM_READ_WRITE, Array.fill(1)(imageFormat), image.getWidth, image.getHeight, 0, null, null)
        clEnqueueWriteImage(queue, inputBuffer, true, Array[Long](0, 0, 0), Array(image.getWidth.toLong, image.getHeight.toLong, 1), image.getWidth * Sizeof.cl_uint, 0, image.pointer(), 0, null, null)

        // This buffer will track which buffer we should finally copy to the image
        var copyBuffer = outputBuffer
        while (kernelQueue.size() != 0) {
            copyBuffer = outputBuffer
            val kernel = kernelQueue.take()
            clSetKernelArg(kernel.kernel, 0, Sizeof.cl_mem, Pointer.to(inputBuffer))
            clSetKernelArg(kernel.kernel, 1, Sizeof.cl_mem, Pointer.to(outputBuffer))
            clEnqueueNDRangeKernel(queue, kernel.kernel, 2, null, Array(image.getWidth.toLong, image.getHeight.toLong), null, 0, null, null)

            val temp = inputBuffer
            inputBuffer = outputBuffer
            outputBuffer = temp
        }

        clEnqueueReadImage(queue, copyBuffer, true, Array[Long](0, 0, 0), Array(image.getWidth.toLong, image.getHeight.toLong, 1), image.getWidth * Sizeof.cl_uint, 0, image.pointer(), 0, null, null)
        clFinish(queue)

        if (inputBuffer != null) {
            clReleaseMemObject(inputBuffer)
            clReleaseMemObject(outputBuffer)
        }
    } .getOrElse(())
}
