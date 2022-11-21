import scala.swing.*
import scala.swing.Swing.*
import scala.swing.event.*
import javax.swing.UIManager

object Main extends SwingApplication {
  val webcam = new Webcam()
  val posterizer = new Posterizer()
  val morpholizer = new Morpholizer()
  val processed_feed = new ProcessedFeed(webcam, posterizer, morpholizer)
  val database = Database("resource/clean.json")
  val recognizer = new Recognizer(processed_feed, database)


  def top(): Frame = new MainFrame {
    title = "Card Scanner"
    contents = new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new VideoPanel(webcam, 30, "Raw Feed")
        contents += new VideoPanel(processed_feed, 30, "Filtered Feed")
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new BoxPanel(Orientation.Vertical) {
          contents += new PosterizerPanel(posterizer)
          contents += new MorpholizerPanel(morpholizer)
        }
        contents += new RecognizerPanel(recognizer)
      }
    }
  }

  override def startup(args: Array[String]): Unit = {
    println("Starting Application...")
    val frame = top()
    if (frame.size == new Dimension(0, 0)) frame.pack()
    frame.visible = true
    frame.resizable = false
    frame.centerOnScreen()
  }

  override def shutdown(): Unit = {
    processed_feed.close()
    webcam.close()
    posterizer.close()
    morpholizer.close()
  }
}