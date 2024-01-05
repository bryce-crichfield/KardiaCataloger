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


