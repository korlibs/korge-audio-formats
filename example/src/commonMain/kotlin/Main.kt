import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*

suspend fun main() = Korge {
    sceneContainer().changeTo({ MainMODScene() })
}
