package com.mteam.calibrationset

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.mteam.calibrationset.Model.RawData
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    //Current Status
    var currentNumber = 1
    var currentSession = 1
    var currentFlick = 1

    var currentX = 0.0
    var currentY = 0.0
    var currentZ = 0.0

    var currentRawX = 0.0
    var currentRawY = 0.0

    var currentTouchPreassure = 0.0
    var currentTouchSize = 0.0

    var isTimer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupSensor()
        setupUI()
        timer()
    }

    fun stateButtonDidTap(view: View) {
        isTimer = !isTimer
    }

    fun timer(){
        var count = 1

        val timer = Timer()
        //Set the schedule function
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    if (isTimer) {
                        addRecordData(false)
                        //println("This is ${count++}")
                    }
                }
            },
            0, 1000
        )

    }

    fun setupUI() {

        //CurrentStatus
        currentNumberClickedTextView.text = currentNumber.toString()
        currentTotalFlickTextView.text = currentFlick.toString()

        //CurrentSensor
        currentSensorXTextView.text = "${currentX}"
        currentSensorYTextView.text = "${currentY}"
        currentSensorZTextView.text = "${currentZ}"

        currentSensorXRawTextView.text = "${currentRawX}"
        currentSensorYRawTextView.text = "${currentRawY}"

        currentTouchPreassureTextView.text = "${currentTouchPreassure}"
        currentTouchSizeTextView.text = "${currentTouchSize}"

        changeTextMainButton()
        hideManualButton()
    }

    fun changeTextMainButton() {
        if (isTimer) {
            stateButton.text = "End Collecting"
        } else {
            stateButton.text = "Start Collecting"
        }
    }

    fun hideManualButton() {
        if (isTimer) {
            testButton1.setVisibility(View.INVISIBLE)
            testButton2.setVisibility(View.INVISIBLE)
            testButton3.setVisibility(View.INVISIBLE)
            testButton4.setVisibility(View.INVISIBLE)
            testButton5.setVisibility(View.INVISIBLE)
        } else {
            testButton1.setVisibility(View.VISIBLE)
            testButton2.setVisibility(View.VISIBLE)
            testButton3.setVisibility(View.VISIBLE)
            testButton4.setVisibility(View.VISIBLE)
            testButton5.setVisibility(View.VISIBLE)
        }
    }

    private val DEBUG_TAG = "Velocity"
    private var mVelocityTracker: VelocityTracker? = null


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val evenAction = event?.action

        currentRawX = event?.getX()?.toDouble() ?: 0.0
        currentRawY = event?.getY()?.toDouble() ?: 0.0

        currentTouchPreassure = event?.getPressure()?.toDouble() ?: 0.0
        currentTouchSize = event?.getSize()?.toDouble() ?: 0.0

        when (evenAction) {
            MotionEvent.ACTION_DOWN -> {
                // Reset the velocity tracker back to its initial state.
                mVelocityTracker?.clear()
                // If necessary retrieve a new VelocityTracker object to watch the
                // velocity of a motion.
                mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
                // Add a user's movement to the tracker.
                mVelocityTracker?.addMovement(event)
            }

            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker?.apply {
                    val pointerId: Int = event.getPointerId(event.actionIndex)
                    addMovement(event)
                    // When you want to determine the velocity, call
                    // computeCurrentVelocity(). Then call getXVelocity()
                    // and getYVelocity() to retrieve the velocity for each pointer ID.
                    computeCurrentVelocity(1000)
                    // Log velocity of pixels per second
                    // Best practice to use VelocityTrackerCompat where possible.

                    //println("${getXVelocity(pointerId)}")
                    //println("${getYVelocity(pointerId)}")

                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                addRecordData(true)

                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker?.recycle()
                mVelocityTracker = null


                //Print current flick status
                println(
                    "User did tap for touch" +
                            "RawX: ${currentRawX} RawY: ${currentRawY} " +
                            "Pitch: ${currentX} Roll: ${currentY} Azimuth: ${currentZ} " +
                            "Touch Preassure: ${currentTouchPreassure} Touch Size: ${currentTouchSize}"
                )

                currentFlick += 1
            }
        }

        return super.dispatchTouchEvent(event)
    }

    fun addRecordData(isTouch: Boolean) {

        //Append Data to RawData
        var data = RawData(
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0
        )


        if (isTouch) {
            data = RawData(
                currentX,
                currentY,
                currentZ,
                currentRawX,
                currentRawY,
                currentTouchPreassure,
                currentTouchSize
            )
        } else {
            data = RawData(
                currentX,
                currentY,
                currentZ,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }

        println(
            "Raw data collected for touch " +
                    "RawX: ${data.rawX} RawY: ${data.rawY} " +
                    "Pitch: ${data.pitch} Roll: ${data.roll} Azimuth: ${data.azimuth} " +
                    "Touch Preassure: ${data.touchPreassure} Touch Size: ${data.touchSize}"
        )

        rawData += data
    }

    private lateinit var sensorManager: SensorManager
    private var mLight: Sensor? = null

    override fun onResume() {
        super.onResume()
        mLight.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        writeCsvFile()
    }

    fun setupSensor() {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        //currentSensorXTextView.text = "${event?.values}"
        currentX = event?.values?.get(0)?.toDouble() ?: 0.0
        currentY = event?.values?.get(1)?.toDouble() ?: 0.0
        currentZ = event?.values?.get(2)?.toDouble() ?: 0.0

        setupUI()
    }

    var rawData = listOf<RawData>()

    fun printRawData() {
        println("Printing Raw Data")
        for (data in rawData) {
            println(data)
        }
    }

    fun requestWriteExternalPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            println("User Permission Granted yeay 🎉")
        }
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        return currentDate
    }

    fun writeCsvFile() {
        val CSV_HEADER = "Pitch,Roll,Azimuth,RawX,RawY,TouchPreassure,TouchSize"
        var fileWriter: FileWriter? = null

        try {
            val currentTime = getCurrentTime().toString()
            val dir = File("/sdcard/collector/").mkdirs()
            fileWriter = FileWriter(File("/sdcard/collector/", "CollectorRawData_${currentTime}.csv"))
            fileWriter.append(CSV_HEADER)
            fileWriter.append('\n')
            for (data in rawData) {
                println("Write ${data.pitch}, ${data.roll}, ${data.azimuth}, ${data.rawX}, ${data.rawX}, ${data.rawY}, ${data.touchPreassure}, ${data.touchSize}")
                fileWriter.append("${data.pitch}")
                fileWriter.append(',')
                fileWriter.append("${data.roll}")
                fileWriter.append(',')
                fileWriter.append("${data.azimuth}")
                fileWriter.append(',')
                fileWriter.append("${data.rawX}")
                fileWriter.append(',')
                fileWriter.append("${data.rawY}")
                fileWriter.append(',')
                fileWriter.append("${data.touchPreassure}")
                fileWriter.append(',')
                fileWriter.append("${data.touchSize}")
                fileWriter.append('\n')
            }
            println("Write CSV successfully!")
        } catch (e: Exception) {
            println("Writing CSV error!")
            e.printStackTrace()
        } finally {
            try {
                fileWriter?.flush()
                fileWriter?.close()
            } catch (e: IOException) {
                println("Flushing/closing error!")
                e.printStackTrace()
            }
        }
    }
}