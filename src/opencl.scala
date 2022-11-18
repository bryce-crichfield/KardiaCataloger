import org.jocl.*
import org.jocl.CL.*
import java.awt.Image
import java.awt.image.{BufferedImage, DataBufferInt, DataBufferByte}
import java.nio.{Buffer, ByteOrder}
import scala.io.Source
import util.{copy, pointer, toInt}

abstract class Kernel() {
  CL.setExceptionsEnabled(true)
  protected val source_path: String
  protected val platform: cl_platform_id = {
    val num_platforms = {
      val array = new Array[Int](1)
      clGetPlatformIDs(0, null, array)
      array.head
    }
    val platforms = new Array[cl_platform_id](num_platforms)
    clGetPlatformIDs(platforms.length, platforms, null)
    platforms.head
  }

  protected val device: cl_device_id = {
    val num_devices = {
      val array = new Array[Int](1)
      clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, array)
      array.head
    }
    val devices = new Array[cl_device_id](num_devices)
    clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, num_devices, devices, null)
    devices.head
  }

  val context: cl_context = {
    val properties = new cl_context_properties()
    properties.addProperty(CL_CONTEXT_PLATFORM, platform)
    val array = Array.fill[cl_device_id](1)(device)
    clCreateContext(properties, 1, array, null, null, null)
  }

  val queue: cl_command_queue = {
    val properites = new cl_queue_properties()
    clCreateCommandQueueWithProperties(context, device, properites, null)
  }

  val kernel: cl_kernel = {
    val code = Source.fromFile(source_path).getLines.mkString
    val source_pointer = Array(code)
    val program =
      clCreateProgramWithSource(context, 1, source_pointer, null, null)
    clBuildProgram(program, 0, null, null, null, null)
    clCreateKernel(program, "main", null)
  }

  def close(): Unit = {
    clReleaseCommandQueue(queue)
    clReleaseContext(context)
  }
}

class ImageKernel(override val source_path: String) extends Kernel {
  private val image_format = {
    val format = new cl_image_format()
    format.image_channel_data_type = CL_UNSIGNED_INT8
    format.image_channel_order = CL_RGBA
    format
  }

  def run(input: BufferedImage, output: BufferedImage)(
      args: cl_kernel => Unit = _ => ()
  ): Unit = {
    assert(input.getWidth() == output.getWidth())
    assert(input.getHeight() == output.getHeight())
    val (width, height) = (input.getWidth(), output.getHeight())

    val input_buffer: cl_mem = {
      clCreateImage2D(
        context,
        CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
        Array.fill(1)(image_format),
        width,
        height,
        width * Sizeof.cl_uint,
        input.pointer(),
        null
      )
    }

    val output_buffer: cl_mem = {
      clCreateImage2D(
        context,
        CL_MEM_WRITE_ONLY,
        Array.fill(1)(image_format),
        width,
        height,
        0,
        null,
        null
      )
    }

    clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(input_buffer))
    clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(output_buffer))
    args(this.kernel)

    clEnqueueNDRangeKernel(
      queue,
      kernel,
      2,
      null,
      Array(width.toLong, height.toLong),
      null,
      0,
      null,
      null
    )
    clEnqueueReadImage(
      queue,
      output_buffer,
      true,
      Array[Long](0, 0, 0),
      Array(width, height, 1),
      width * Sizeof.cl_uint,
      0,
      output.pointer(),
      0,
      null,
      null
    )
    clReleaseMemObject(input_buffer)
    clReleaseMemObject(output_buffer)
  }

  override def close(): Unit = {
    super.close()
  }
}


