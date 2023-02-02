import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*

suspend fun main() = Korge {
    sceneContainer().changeTo({ MainMODScene() })
}
