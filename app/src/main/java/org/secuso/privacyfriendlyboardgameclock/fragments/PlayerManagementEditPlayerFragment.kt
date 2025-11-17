/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Board Game Clock is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Board Game Clock. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.model.Player

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This Fragment opens up a dialog from which user can edit existing players
 */
class PlayerManagementEditPlayerFragment : DialogFragment() {
    private var activity: Activity? = null
    private var rootView: View? = null
    private var playerIcon: Bitmap? = null
    private var playerName: EditText? = null
    private var pictureIMGView: ImageView? = null
    private var colorIMGView: ImageView? = null
    private val p: Player? = null
    var confirmButtonListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            p!!.setName(playerName!!.getText().toString())
            pictureIMGView!!.buildDrawingCache()
            playerIcon = pictureIMGView!!.getDrawingCache()
            p.icon = playerIcon

            //            pds.updatePlayer(p);
            activity!!.onBackPressed()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity = getActivity()
        val builder = AlertDialog.Builder(activity)
            .setTitle(R.string.editPlayer)
            .setPositiveButton(
                R.string.confirm,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        p!!.setName(playerName!!.getText().toString())
                        pictureIMGView!!.buildDrawingCache()
                        playerIcon = pictureIMGView!!.getDrawingCache()
                        p.icon = playerIcon
                        //                                pds.updatePlayer(p);
                        // reload the activity starting this
                        val intent = getActivity()!!.getIntent()
                        getActivity()!!.finish()
                        startActivity(intent)
                    }
                }
            )
            .setNeutralButton(R.string.playerStatistic, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    val fm = getFragmentManager()
                    val ft = fm!!.beginTransaction()
                    val prev = fm.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT)
                    if (prev != null) ft.remove(prev)
                    ft.addToBackStack(null)

                    // Create and show the dialog
                    val playerStatisticFragment =
                        PlayerManagementStatisticsFragment.newInstance("Player Statistic")
                    playerStatisticFragment.show(ft, TAGHelper.DIALOG_FRAGMENT)
                }
            })
        val inflater = activity!!.getLayoutInflater()
        rootView = inflater.inflate(R.layout.fragment_player_management_newplayer, null)


        //        p = ((PlayerManagementActivity)activity).playerToEdit;
        playerName = rootView!!.findViewById<EditText>(R.id.editName)
        playerName!!.setInputType(InputType.TYPE_CLASS_TEXT)
        playerName!!.setText(p!!.getName())

        playerIcon = p.icon
        pictureIMGView = rootView!!.findViewById<View?>(R.id.picture) as ImageView
        pictureIMGView!!.setImageBitmap(playerIcon)

        colorIMGView = rootView!!.findViewById<ImageView>(R.id.color)
        colorIMGView!!.setOnClickListener(colorWheelDialog())

        if (activity!!.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            pictureIMGView!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_REQUEST)
                }
            })
        }

        builder.setView(rootView)
        return builder.create()
    }

    private fun colorWheelDialog(): View.OnClickListener {
        return object : View.OnClickListener {
            override fun onClick(v: View?) {
                ColorPickerDialogBuilder
                    .with(activity)
                    .setTitle("Choose color")
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener(object : OnColorSelectedListener {
                        override fun onColorSelected(selectedColor: Int) {
                        }
                    })
                    .setPositiveButton("OK", object : ColorPickerClickListener {
                        override fun onClick(
                            dialog: DialogInterface?,
                            selectedColor: Int,
                            allColors: Array<Int?>?
                        ) {
                            pictureIMGView!!.setImageBitmap(playerIcon)
                            pictureIMGView!!.setColorFilter(selectedColor, PorterDuff.Mode.OVERLAY)
                            colorIMGView!!.setBackgroundColor(selectedColor)
                        }
                    })
                    .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        }
                    })
                    .build()
                    .show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val photo = imageReturnedIntent!!.getExtras()!!.get("data") as Bitmap?
            val picture = rootView!!.findViewById<View?>(R.id.picture) as ImageView

            playerIcon = Bitmap.createScaledBitmap(cutSquareBitmap(photo!!), 288, 288, false)
            picture.setImageBitmap(playerIcon)
        }
    }

    private fun cutSquareBitmap(b: Bitmap): Bitmap {
        val bHeight = b.getHeight()
        val bWidth = b.getWidth()
        var longEdge = bHeight
        var shortEdge = bWidth

        if (bWidth > bHeight) {
            longEdge = bWidth
            shortEdge = bHeight
        }

        val diff = longEdge - shortEdge

        return Bitmap.createBitmap(b, 0, diff / 2, shortEdge, shortEdge)
    }

    companion object {
        private const val CAMERA_REQUEST = 1888
        fun newInstance(title: String?): PlayerManagementEditPlayerFragment {
            val frag = PlayerManagementEditPlayerFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)
            return frag
        }
    }
}
