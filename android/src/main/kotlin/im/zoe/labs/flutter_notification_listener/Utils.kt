package im.zoe.labs.flutter_notification_listener

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.app.RemoteInput
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.service.notification.StatusBarNotification
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest

class Utils {
    companion object {
        fun Drawable.toBitmap(): Bitmap {
            if (this is BitmapDrawable) {
                return this.bitmap
            }

            val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            this.setBounds(0, 0, canvas.width, canvas.height)
            this.draw(canvas)

            return bitmap
        }

        fun md5(input:String): String {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
        }


        fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }
    }

    class Marshaller {

        val convertorFactory = HashMap<Class<*>, Convertor>()

        init {
            // improve performance
            register<String> { passConvertor(it) }
            register<Int> { passConvertor(it) }
            register<Long> { passConvertor(it) }
            register<Float> { passConvertor(it) }
            register<Boolean> { passConvertor(it) }

            // basic types
            register<CharSequence> { it.toString() }

            // collections type
            register<List<*>> { arrayConvertor(it as List<*>) }
            // register<Array<*>> { arrayConvertor(it as Array<*>) }
            register<Array<*>> { obj ->
                val items = mutableSetOf<Any?>()
                (obj as Array<*>).forEach {
                    items.add(marshal(it))
                }
                items
            }

            // extends type
            register<StatusBarNotification> {
                val v = it as StatusBarNotification
                val map = HashMap<String, Any?>()
                map["id"] = v.id
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    map["groupKey"] = v.groupKey
                }
                map["isClearable"] = v.isClearable
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    map["isGroup"] = v.isGroup
                }
                map["isOngoing"] = v.isOngoing
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    map["key"] = v.key
                }
                map["packageName"] = v.packageName
                map["postTime"] = v.postTime
                map["tag"] = v.tag
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    map["uid"] = v.uid
                }
                map["notification"] = marshal(v.notification)
                map
            }
            register<Notification> {
                val v = it as Notification
                val map = HashMap<String, Any?>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    map["channelId"] = v.channelId
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    map["category"] = v.category
                }
                map["extras"] = marshal(v.extras)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    map["color"] = v.color
                }
                map["contentIntent"] = marshal(v.contentIntent)
                map["deleteIntent"] = marshal(v.deleteIntent)
                map["fullScreenIntent"] = marshal(v.fullScreenIntent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    map["group"] = v.group
                }
                map["actions"] = marshal(v.actions)
                map["when"] = v.`when`
                map["tickerText"] = marshal(v.tickerText)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    map["tickerText"] = marshal(v.settingsText)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    map["timeoutAfter"] = v.timeoutAfter
                }
                map["number"] = v.number
                map
            }
            register<PendingIntent> {
                val v = it as PendingIntent
                val map = HashMap<String, Any?>()
                map["creatorPackage"] = v.creatorPackage
                map["creatorUid"] = v.creatorUid
                map
            }

            register<Bitmap> { convertBitmapToByteArray(it as Bitmap) }
            register<Bundle> { obj ->
                val v = obj as Bundle
                val map = HashMap<String, Any?>()
                val keys = obj.keySet()
                keys.forEach { map[it] = marshal(v.get(it)) }
                map
            }
            register<Notification.Action> { obj ->
                val v = obj as Notification.Action
                val map = HashMap<String, Any?>()
                map["title"] = v.title
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    map["semantic"] = v.semanticAction
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    map["inputs"] = marshal(v.remoteInputs)
                    map["extras"] = marshal(v.extras)
                }
                map
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                register<RemoteInput> {
                    val v = it as RemoteInput
                    val map = HashMap<String, Any?>()
                    map["label"] = v.label
                    map["resultKey"] = v.resultKey
                    map["choices"] = marshal(v.choices)
                    map["extras"] = marshal(v.extras)
                    map
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                register<Icon> { ignoreConvertor(it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                register<Notification.MessagingStyle> {
                    val v = it as Notification.MessagingStyle
                    val map = HashMap<String, Any?>()
                    map["conversationTitle"] = marshal(v.conversationTitle)
                    map["messages"] = marshal(v.messages)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        map["user"] = marshal(v.user)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        map["historicMessages"] = marshal(v.historicMessages)
                    }
                    map
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                register<Person> {
                    val v = it as Person
                    val map = HashMap<String, Any?>()
                    map["isBot"] = v.isBot
                    map["isImportant"] = v.isImportant
                    map["key"] = v.key
                    map["name"] = v.name
                    map["uri"] = v.uri
                    map
                }
            }
        }

        private fun <T> arrayConvertor(obj: T): List<Any?>
            where T: List<*> {
            val items = mutableListOf<Any?>()
            (obj as List<*>).forEach { items.add(marshal(it)) }
            return items
        }

        private fun ignoreConvertor(obj: Any?): Any? {
            return null
        }

        private fun passConvertor(obj: Any?): Any? {
            return obj
        }

        inline fun <reified T> register(noinline fn: Convertor) {
            convertorFactory[T::class.java] = fn
        }

        fun marshal(obj: Any?): Any? {
            if (obj == null) return null
            // get the type of obj? and return
            // can we use get directly?
            for (et in convertorFactory) {
                if (et.key.isAssignableFrom(obj.javaClass)) {
                    return et.value.invoke(obj)
                }
            }
            return obj
        }

        companion object {
            val instance = Marshaller()

            fun marshal(obj: Any?): Any? {
                return instance.marshal(obj)
            }

            inline fun <reified T> register(noinline fn: Convertor) {
                return instance.register<T>(fn)
            }
        }
    }
}

typealias Convertor = (Any) -> Any?