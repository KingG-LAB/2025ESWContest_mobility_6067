package com.example.emergencypriority.mqtt

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttManager(
    context: Context,
    private val brokerUrl: String,
    private val clientId: String
) {
    private val mqttClient = MqttAndroidClient(context, brokerUrl, clientId)

    fun connect(onConnected: () -> Unit = {}) {
        val options = org.eclipse.paho.client.mqttv3.MqttConnectOptions().apply {
            isCleanSession = true
//          userName = "test"
//          password = "1234".toCharArray()
        }
        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                onConnected()
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                exception?.printStackTrace()
            }
        })
    }

    fun publish(topic: String, payload: String) {
        val msg = MqttMessage(payload.toByteArray())
        msg.qos = 0
        mqttClient.publish(topic, msg)
    }

    fun setCallback(callback: MqttCallback) {
        mqttClient.setCallback(callback)
    }
}
