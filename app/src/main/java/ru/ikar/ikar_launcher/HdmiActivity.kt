//package ikar.app.ikar_launcher
//
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.media.tv.TvContract
//import android.media.tv.TvInputInfo
//import android.media.tv.TvInputManager
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import kotlinx.android.synthetic.main.activity_main.*
//import java.util.ArrayList
//
//class MainActivity : AppCompatActivity() {
//
//    companion object {
//        const val TAG = "DeviceTypeRuntimeCheck"
//    }
//
//    private val contractList: MutableList<Uri> = ArrayList()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        button.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, contractList[0])
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//
//        button2.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, contractList[1])
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//
//        button3.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, contractList[2])
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//
//        val isTelevision = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
//        if (isTelevision) {
//            Log.d(TAG, "Running on a TV Device")
//        } else {
//            Log.d(TAG, "Running on a non-TV Device")
//        }
//
//        val tvInputManager = getSystemService(TV_INPUT_SERVICE) as TvInputManager
//        var contract: String? = null
//        for (tvInputInfo in tvInputManager.tvInputList) {
//            when (tvInputInfo.type) {
//                TvInputInfo.TYPE_HDMI -> {
//                    Log.d(TAG, "TYPE_HDMI ")
//                    contract += TvContract.buildChannelUriForPassthroughInput(tvInputInfo.id).toString() + "\n"
//                    contractList.add(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.id))
//                }
//                TvInputInfo.TYPE_DISPLAY_PORT -> Log.d(TAG, "TYPE_DISPLAY_PORT")
//                TvInputInfo.TYPE_TUNER -> Log.d(TAG, "TYPE_TUNER")
//            }
//        }
//
//        TT.text = contractList.size.toString()
//    }
//}
