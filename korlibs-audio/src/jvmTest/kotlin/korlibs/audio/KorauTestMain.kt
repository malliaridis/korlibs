package korlibs.audio

import korlibs.audio.sound.readMusic
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking

object KorauTestMain  {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val music = resourcesVfs["res/mp31_joint_stereo_vbr.mp3"].readMusic()
        }
    }
}
