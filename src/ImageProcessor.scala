import org.jocl.*
import org.jocl.CL.*
import java.awt.Image
import java.awt.image.{BufferedImage, DataBufferInt, DataBufferByte}
import java.nio.Buffer
import scala.io.Source
import util.{copy}

trait Kernel {
  CL.setExceptionsEnabled(true)

  protected lazy val source_path: String

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

// Describes a function run on the GPU of the basic form
// input_buffer: cl_mem => output_buffer: cl_mem
// The user must specify how to construct the input/output buffers
// As well as how to apply the function
// trait KernelFunction extends Kernel {
//   protected lazy val input_buffer: cl_mem
//   protected lazy val output_buffer: cl_mem

//   protected def execute(worksize: Array[Long]): Unit = {
//     clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(input_buffer))
//     clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(output_buffer))
//     clEnqueueNDRangeKernel(
//       queue,
//       kernel,
//       worksize.length,
//       null,
//       worksize,
//       null,
//       0,
//       null,
//       null
//     )
//   }

//   override def close(): Unit = {
//     super.close()
//     clReleaseMemObject(input_buffer)
//     clReleaseMemObject(output_buffer)
//   }
// }

class ImageKernel(width: Int, height: Int) extends Kernel {
  override protected lazy val source_path: String =
    "/home/bryce/Desktop/card_scanner/kernel/id.cl"
  private val image_format = {
    val format = new cl_image_format()
    format.image_channel_data_type = CL_UNSIGNED_INT8
    format.image_channel_order = CL_RGBA
    format
  }
  private val in_buffered_image: BufferedImage =
    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
  private val in_raster_pointer = Pointer.to(
    in_buffered_image
      .getRaster()
      .getDataBuffer()
      .asInstanceOf[DataBufferInt]
      .getData()
  )

  private val out_buffered_image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
  private val out_raster_pointer = Pointer.to(
    out_buffered_image
      .getRaster()
      .getDataBuffer()
      .asInstanceOf[DataBufferInt]
      .getData()
  )
  private val output_buffer: cl_mem = {
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

  // Loads BufferedImage into host/device pinned memory pair
  // for gpu operation
  def toDevice(image: BufferedImage): Unit = {
    val graphics = in_buffered_image.getGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()
  }

  // Loads host/device pinned memory pait into provided 
  // buffered image
  def toHost(image: BufferedImage): Unit = {
    val graphics = image.getGraphics()
    graphics.drawImage(out_buffered_image, 0, 0, null)
    graphics.dispose()
  }

  def run(): Unit = {
    // The input buffer fails to persist.  I believe the reason is that
    // the host memory must first be mapped.  See clEnqueueMapBuffer
    val input_buffer: cl_mem = {
      clCreateImage2D(
        context,
        CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
        Array.fill(1)(image_format),
        width,
        height,
        width * Sizeof.cl_uint,
        in_raster_pointer,
        null
      )
    }
    clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(input_buffer))
    clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(output_buffer))
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
      out_raster_pointer,
      0,
      null,
      null
    )
    clReleaseMemObject(input_buffer)
  }

  override def close(): Unit = {
    super.close()
    clReleaseMemObject(output_buffer)
  }
}

class ProcessedFeed(input: VideoFeed) extends VideoFeed {
  var alpha: Int = 0
  var beta: Int = 0
  var min: Int = 0
  var max: Int = 255

  private var width: Int = 0
  private var height: Int = 0
  private lazy val kernel: ImageKernel = new ImageKernel(width, height)
  

  override def apply(function: BufferedImage => Unit): Unit = {
    input { input =>
      width = input.getWidth()
      height = input.getHeight()
      val output = input.copy()
      kernel.toDevice(input)
      kernel.run()
      kernel.toHost(output)
      function(output)
    }
  }

  override def close(): Unit = {
    input.close()
    kernel.close()
  }
}
