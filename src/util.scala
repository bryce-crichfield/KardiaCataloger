import java.awt.image.BufferedImage

object util {
  inline def rate(fps: Int): Int = 1000/fps

  


  extension (image: BufferedImage)
    def copy(format: Int = BufferedImage.TYPE_INT_RGB): BufferedImage = {
      val buffer = new BufferedImage (
        image.getWidth(), 
        image.getHeight(), 
        BufferedImage.TYPE_INT_RGB
      )
      val graphics = buffer.createGraphics()
      graphics.drawImage(image, 0, 0, null)
      graphics.dispose()
      buffer
    }
}
