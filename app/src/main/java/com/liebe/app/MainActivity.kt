package com.liebe.app

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max

class Item(val asset: String? = null, val uri: Uri? = null)

class MainActivity : Activity() {

    private val items = mutableListOf<Item>()
    private val texts = mutableListOf<String>()
    private var index = 0
    private var base: Bitmap? = null
    private var version = 0
    private var suppress = false
    private val ui = Handler(Looper.getMainLooper())
    private val refresh = Runnable { renderCurrent() }

    private lateinit var preview: ImageView
    private lateinit var counter: TextView
    private lateinit var textEdit: EditText
    private lateinit var nameEdit: EditText

    private val prefs by lazy { getSharedPreferences("liebe", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        loadEmbedded()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    // ---------- UI ----------

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { onClick() }
    }

    private fun row(vararg bs: Button) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        bs.forEach { addView(it, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)) }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(16))
        }

        root.addView(TextView(this).apply {
            text = "❤ Liebesbilder"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        })

        nameEdit = EditText(this).apply {
            hint = "Name deiner Freundin (optional)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setText(prefs.getString("name", ""))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    prefs.edit().putString("name", s.toString()).apply()
                }
            })
        }
        root.addView(nameEdit)

        root.addView(row(
            button("❤ Unsere Bilder") { loadEmbedded() },
            button("📷 Eigene Bilder") { pick() }
        ))

        preview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFFEEEEEE.toInt())
        }
        root.addView(preview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(420)).apply {
            topMargin = dp(8)
        })

        counter = TextView(this).apply {
            gravity = Gravity.CENTER
            text = ""
        }
        root.addView(counter)

        textEdit = EditText(this).apply {
            hint = "Text zum Bild (kannst du auch selbst ändern)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (suppress || items.isEmpty()) return
                    texts[index] = s.toString()
                    ui.removeCallbacks(refresh)
                    ui.postDelayed(refresh, 500)
                }
            })
        }
        root.addView(textEdit)

        root.addView(row(
            button("◀") { step(-1) },
            button("🔄 Neuer Text") { newText() },
            button("▶") { step(1) }
        ))
        root.addView(row(
            button("💾 Speichern") { save() },
            button("💌 Teilen") { share() }
        ))
        root.addView(button("💾 Alle Bilder speichern") { saveAll() })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    // ---------- Bilder laden ----------

    private fun name() = nameEdit.text.toString()

    private fun pick() {
        val i = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(Intent.createChooser(i, "Bilder auswählen"), 1)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1 || resultCode != RESULT_OK || data == null) return
        val list = mutableListOf<Uri>()
        val clip = data.clipData
        if (clip != null) {
            for (k in 0 until clip.itemCount) list.add(clip.getItemAt(k).uri)
        } else data.data?.let { list.add(it) }
        setImages(list)
    }

    @Suppress("DEPRECATION")
    private fun handleIntent(i: Intent?) {
        if (i == null) return
        val list = mutableListOf<Uri>()
        when (i.action) {
            Intent.ACTION_SEND -> {
                val u = if (Build.VERSION.SDK_INT >= 33)
                    i.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else i.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (u != null) list.add(u)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val l = if (Build.VERSION.SDK_INT >= 33)
                    i.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else i.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (l != null) list.addAll(l)
            }
        }
        setImages(list)
    }

    private fun loadEmbedded() {
        items.clear(); texts.clear()
        Messages.photoTexts.forEachIndexed { i, t ->
            items.add(Item(asset = "photos/%02d.jpg".format(i + 1)))
            texts.add(Messages.fill(t, name()))
        }
        index = 0
        showIndex()
    }

    private fun setImages(list: List<Uri>) {
        if (list.isEmpty()) return
        items.clear(); items.addAll(list.map { Item(uri = it) })
        texts.clear(); repeat(list.size) { texts.add(Messages.next(name())) }
        index = 0
        showIndex()
    }

    private fun step(d: Int) {
        if (items.isEmpty()) return
        index = (index + d + items.size) % items.size
        showIndex()
    }

    private fun newText() {
        if (items.isEmpty()) return
        textEdit.setText(Messages.next(name())) // löst Watcher aus -> Vorschau aktualisiert sich
    }

    private fun showIndex() {
        counter.text = "Bild ${index + 1} von ${items.size}"
        suppress = true
        textEdit.setText(texts[index])
        suppress = false
        val v = ++version
        val item = items[index]
        Thread {
            val b = try { decode(item) } catch (e: Exception) { null }
            runOnUiThread {
                if (v == version) {
                    if (b == null) toast("Bild konnte nicht geladen werden") else {
                        base = b
                        renderCurrent()
                    }
                }
            }
        }.start()
    }

    private fun renderCurrent() {
        val b = base ?: return
        val t = textEdit.text.toString()
        val v = ++version
        Thread {
            val out = render(b, t)
            runOnUiThread { if (v == version) preview.setImageBitmap(out) }
        }.start()
    }

    // ---------- Bild + Text ----------

    private fun decode(item: Item): Bitmap {
        val src = if (item.asset != null) ImageDecoder.createSource(assets, item.asset)
        else ImageDecoder.createSource(contentResolver, item.uri!!)
        return ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetSampleSize(max(1, max(info.size.width, info.size.height) / 1600))
        }
    }

    private fun render(src: Bitmap, text: String): Bitmap {
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        if (text.isBlank()) return bmp
        val c = Canvas(bmp)
        val w = bmp.width
        val h = bmp.height
        val pad = w * 0.06f

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = w * 0.055f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            setShadowLayer(w * 0.008f, 0f, w * 0.003f, Color.BLACK)
        }
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, (w - 2 * pad).toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.15f)
            .build()

        val top = h - layout.height - pad
        val bg = Paint().apply {
            shader = LinearGradient(
                0f, top - pad * 1.5f, 0f, h.toFloat(),
                0x00000000, 0xB3000000.toInt(), Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, top - pad * 1.5f, w.toFloat(), h.toFloat(), bg)
        c.save()
        c.translate(pad, top)
        layout.draw(c)
        c.restore()
        return bmp
    }

    // ---------- Speichern & Teilen ----------

    private fun saveBitmap(bmp: Bitmap, n: Int): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Liebe_${System.currentTimeMillis()}_$n.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Liebesbilder")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        return uri
    }

    private fun currentFinal(): Bitmap? {
        if (items.isEmpty()) return null
        return render(decode(items[index]), texts[index])
    }

    private fun save() {
        if (items.isEmpty()) return toast("Erst ein Bild auswählen")
        Thread {
            val ok = try { saveBitmap(currentFinal()!!, index) != null } catch (e: Exception) { false }
            toast(if (ok) "Gespeichert in Bilder/Liebesbilder ❤️" else "Speichern fehlgeschlagen")
        }.start()
    }

    private fun saveAll() {
        if (items.isEmpty()) return toast("Erst Bilder auswählen")
        Thread {
            var count = 0
            for (k in items.indices) {
                try {
                    if (saveBitmap(render(decode(items[k]), texts[k]), k) != null) count++
                } catch (e: Exception) { }
            }
            toast("$count von ${items.size} Bildern gespeichert ❤️")
        }.start()
    }

    private fun share() {
        if (items.isEmpty()) return toast("Erst ein Bild auswählen")
        Thread {
            val uri = try { saveBitmap(currentFinal()!!, index) } catch (e: Exception) { null }
            runOnUiThread {
                if (uri == null) return@runOnUiThread toast("Teilen fehlgeschlagen")
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(i, "An meine Liebste senden"))
            }
        }.start()
    }

    private fun toast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }
}
