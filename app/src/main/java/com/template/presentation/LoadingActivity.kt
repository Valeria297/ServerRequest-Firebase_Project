package com.template.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.template.R
import okhttp3.*
import java.io.IOException
import java.util.*

class LoadingActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("db")

    private val holder = MutableLiveData<String>()

    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private lateinit var userAgent: String

    private var webLink = ""

    private val lifecycleReg: LifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        firebaseReq()

        val packName = applicationContext.packageName
        val userId = UUID.randomUUID()
        val timeZone = TimeZone.getDefault().id

        holder.observe(this) {
            val serverUrl =
                "$it/?packageid=$packName&userid=$userId&getz=$timeZone" +
                        "&getr=utm_source=google-play&utm_medium=organic"

            Log.d("Url", "url is: $serverUrl ")

            serverReq(serverUrl)
        }
    }

    private fun firebaseReq() {
        lateinit var temp: String

        ref.addValueEventListener(object : ValueEventListener, LifecycleOwner {
            override fun onDataChange(snapshot: DataSnapshot) {
                temp = snapshot.child("link").value.toString()
                holder.value = temp
            }

            override fun onCancelled(error: DatabaseError) {
                val intentOnError = Intent(
                    this@LoadingActivity,
                    MainActivity::class.java
                )
                startActivity(intentOnError)
            }

            override fun getLifecycle(): Lifecycle = lifecycleReg
        })
    }

    private fun serverReq(url: String) {
        userAgent = WebView(this).settings.userAgentString

        client = OkHttpClient.Builder()
            .addNetworkInterceptor {
                it.proceed(
                    it.request()
                        .newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                )
            }.build()

        request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                client.newCall(request).execute()
                    .use {
                        webLink = it
                            .body()?.string()
                            .toString()
                        Log.d(
                            "Response from sever",
                            webLink
                        )

                        val prefs =
                            getSharedPreferences(
                                resources.getString(R.string.app_name),
                                Context.MODE_PRIVATE
                            )
                        val editor = prefs.edit()

                        editor.putString("sURL", webLink)
                        editor.apply()
                    }

                val intentWeb = Intent(
                    this@LoadingActivity,
                    WebActivity::class.java
                )
                startActivity(intentWeb)
            }

            override fun onFailure(call: Call, e: IOException) {
                val intentOnError = Intent(
                    this@LoadingActivity,
                    MainActivity::class.java
                )
                startActivity(intentOnError)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(COUNT_KEY, count)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        count = savedInstanceState.getInt(COUNT_KEY)
    }

    companion object {
        const val COUNT_KEY = "COUNT_KEY"
        private var count = 1
    }
}

