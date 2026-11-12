package fefu.prime.srastem.platform.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

private class PhotoPickerDelegate(
    private val onImageSelected: (String?) -> Unit,
    private val onDismissed: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val path = image?.let {
            val documents = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).firstOrNull() as? String
                ?: return@let null
            val filePath = "$documents/image-${NSDate().timeIntervalSince1970}.jpg"
            UIImageJPEGRepresentation(it, 0.9)?.writeToFile(filePath, true)
            filePath
        }
        picker.dismissViewControllerAnimated(true, completion = null)
        onImageSelected(path?.let { "file://$it" })
        onDismissed()
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onDismissed()
    }
}

@Composable
actual fun PlatformImagePicker(
    onImageSelected: (String?) -> Unit,
    content: @Composable (openPicker: () -> Unit) -> Unit,
) {
    val retainedDelegate = remember { mutableStateOf<PhotoPickerDelegate?>(null) }
    val picker = remember(onImageSelected) {
        {
            val controller = UIImagePickerController()
            val delegate = PhotoPickerDelegate(onImageSelected) { retainedDelegate.value = null }
            retainedDelegate.value = delegate
            controller.delegate = delegate
            controller.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            topViewController(UIApplication.sharedApplication.keyWindow?.rootViewController)
                ?.presentViewController(controller, animated = true, completion = null)
        }
    }
    content(picker as () -> Unit)
}

private fun topViewController(controller: UIViewController?): UIViewController? {
    val presented = controller?.presentedViewController
    return if (presented == null) controller else topViewController(presented)
}
