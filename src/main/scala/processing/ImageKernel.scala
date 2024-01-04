package org.bpc
package processing

import Util.BufferedImageExtension

import org.jocl.CL._
import org.jocl._

import java.awt.image.BufferedImage
import javax.swing.SwingWorker
import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

class ImageKernel private(val kernel: cl_kernel) {
    def setArgInt(index: Int, arg: Int*): Unit = {
        val array = arg.toArray
        clSetKernelArg(kernel, index + 2, Sizeof.cl_int * array.length, Pointer.to(array))
    }

    def setArgFloat(index: Int, arg: Float*): Unit = {
        val array = arg.toArray
        clSetKernelArg(kernel, index + 2, Sizeof.cl_float * array.length, Pointer.to(array))
    }

//    def enqueue(): Future[Unit] = {
//        val worker = new SwingWorker[Unit, Unit] {
//            override def doInBackground(): Unit = {
//                val width = image.getWidth()
//                val height = image.getHeight()
//
//                lazy val input_buffer: cl_mem = {
//                    clCreateImage2D(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Array.fill(1)(image_format), width, height, width * Sizeof.cl_uint, image.pointer(), null)
//                }
//
//                lazy val output_buffer: cl_mem = {
//                    clCreateImage2D(context, CL_MEM_WRITE_ONLY, Array.fill(1)(image_format), width, height, 0, null, null)
//                }
//
//                // copy the input image to the input buffer
//                clEnqueueWriteImage(queue, input_buffer, true, Array[Long](0, 0, 0), Array(width, height, 1), width * Sizeof.cl_uint, 0, image.pointer(), 0, null, null)
//
//                // wait for the input buffer to be written
//                clFinish(queue)
//
//                clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(input_buffer))
//                clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(output_buffer))
//
//                clEnqueueNDRangeKernel(queue, kernel, 2, null, Array(width.toLong, height.toLong), null, 0, null, null)
//
//                // wait for the kernel to finish
//                clFinish(queue)
//
//                // Read the copy the output buffer to the output image
//                clEnqueueReadImage(queue, output_buffer, true, Array[Long](0, 0, 0), Array(width, height, 1), width * Sizeof.cl_uint, 0, image.pointer(), 0, null, null)
//
//                clFinish(queue)
//
//                clReleaseMemObject(input_buffer)
//                clReleaseMemObject(output_buffer)
//            }
//        }
//
//        // await the worker to finish
//        worker.execute()
//        worker.get()
//
//        // return a future that completes when the worker finishes
//        Future.successful(())
//    }
}

object ImageKernel {
    def load(sourcePath: String, context: cl_context): Try[ImageKernel] = Try {
        val path = ImageKernel.getClass().getResource(sourcePath)
        val source = Source.fromURL(path)
        val name = sourcePath.substring(1, sourcePath.lastIndexOf('.'))
        val sourcePointer = Array(source.getLines().mkString)
        val program = clCreateProgramWithSource(context, 1, sourcePointer, null, null)
        clBuildProgram(program, 0, null, null, null, null)
        val kernel = clCreateKernel(program, "main", null)

        source.close()
        clReleaseProgram(program)

        new ImageKernel(kernel)
    }
}


