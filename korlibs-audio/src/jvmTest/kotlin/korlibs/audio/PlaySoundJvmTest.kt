package korlibs.audio

import korlibs.audio.sound.readSound
import korlibs.io.async.suspendTest
import korlibs.io.file.std.resourcesVfs
import org.junit.Test

class PlaySoundJvmTest {
    @Test
    fun testReadNativeSound() = suspendTest {
        val soundWav = resourcesVfs["res/wav1.wav"].readSound()
        val soundMp3 = resourcesVfs["res/fl4.mp1"].readSound()
    }

    //@Test
    //fun test() = suspendTest {
    //    coroutineScope {
    //        //val sound = resourcesVfs["res/mp31.mp3"].readSound()
    //        val sound = resourcesVfs["res/wav1.wav"].readSound()
    //        launchAsap {
    //            sound.playAndWait()
    //        }
    //        launchAsap {
    //            delay(100.milliseconds)
    //            sound.playAndWait()
    //        }
    //        launchAsap {
    //            delay(200.milliseconds)
    //            sound.playAndWait()
    //        }
    //        launchAsap {
    //            delay(300.milliseconds)
    //            sound.playAndWait()
    //        }
    //        launchAsap {
    //            delay(400.milliseconds)
    //            sound.playAndWait()
    //        }
    //        launchAsap {
    //            delay(500.milliseconds)
    //            val channel = sound.play()
    //            channel.pitch = 0.2
    //            channel.await()
    //        }
    //    }
    //}
}
