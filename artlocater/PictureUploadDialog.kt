package us.ait.artlocater

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent.getIntent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.common.collect.Maps
import kotlinx.android.synthetic.main.add_pic_dialog.view.*
import java.lang.IllegalStateException
import java.lang.RuntimeException

class PictureUploadDialog : DialogFragment() {
    interface PictureUploadHandler {
        fun pictureUploadShow(image: Bitmap)
    }

    lateinit var pictureUploadHandler: PictureUploadHandler

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is PictureUploadHandler) {
            pictureUploadHandler = context
        } else {
            throw RuntimeException(getString(R.string.handlererror))
        }
    }

    lateinit var ivPic: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.setTitle(getString(R.string.upload))

        val dialogView = requireActivity().layoutInflater.inflate(R.layout.add_pic_dialog, null)

        ivPic = dialogView.ivPic

        dialogBuilder.setView(dialogView)

        val image = getArguments()?.getParcelable<Bitmap>("Image")

        ivPic.setImageBitmap(image)

//        if (arguments != null) {
//            if (arguments!!.containsKey(MapsActivity.KEY_PIC_SHOW)) {
//
//                //val image = arguments!!.getSerializable(MapsActivity.KEY_PIC_SHOW) as Bitmap
//
//                val image = arguments?.getParcelable<Bitmap>("Image")
//
//                ivPic.setImageBitmap(image)
//            }
//        }

        dialogBuilder.setPositiveButton(getString(R.string.submit)) { dialog, which ->
            (context as MapsActivity).uploadPhoto()
        }

        dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->

        }

        return dialogBuilder.create()
    }

    //
    override fun show(manager: FragmentManager?, tag: String?) {

        try {
            var ft: FragmentTransaction = manager!!.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
            Log.d(getString(R.string.committed), getString(R.string.msg))
        } catch (e: IllegalStateException) {
            Log.d(getString(R.string.errors), getString(R.string.exception), e)
        }
    }
}