package tn.isetbizerte.tp3_channels

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** Exercice 2 : allume ou eteint la lampe torche (aucune permission necessaire). */
class Torche(context: Context) : MethodChannel.MethodCallHandler {

    private val cameraManager = context.getSystemService(CameraManager::class.java)

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "allumer" -> {
                val etat = call.argument<Boolean>("etat")
                if (etat == null) {
                    result.error("ARGUMENT_MANQUANT", "Il faut envoyer un booléen « etat »", null)
                    return
                }
                try {
                    // Cherche la camera qui possede un flash
                    val id = cameraManager.cameraIdList.firstOrNull { cameraId ->
                        cameraManager.getCameraCharacteristics(cameraId)
                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    }
                    if (id == null) {
                        result.error("PAS_DE_FLASH", "Cet appareil n'a pas de flash", null)
                        return
                    }
                    cameraManager.setTorchMode(id, etat)
                    result.success(null)
                } catch (e: CameraAccessException) {
                    result.error("CAMERA_INDISPONIBLE", e.message, null)
                }
            }
            else -> result.notImplemented()
        }
    }
}
