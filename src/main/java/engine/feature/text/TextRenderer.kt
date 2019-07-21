package engine.feature.text

import com.jogamp.opengl.GL4
import com.jogamp.opengl.util.texture.Texture
import engine.core.OpenGlObject
import engine.feature.shader.Shader
import engine.feature.texture.TextureLoader
import engine.util.utilgeometry.PointF

import java.awt.*
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

class TextRenderer private constructor(private val textureAtlas: Texture?,
                                       private val characterCoordinates: HashMap<Char, PointF>?,
                                       private val charSize: Dimension) {

    private val cache: HashMap<Char, OpenGlObject> = HashMap()

    val isValid: Boolean
        get() = this.textureAtlas != null && this.characterCoordinates != null

    private fun drawCharacter(c: Char, fontSize: Dimension, gl: GL4, pos: PointF, shader: Shader) {
        val glObject: OpenGlObject

        if (!cache.containsKey(c)) {
            glObject = OpenGlObject(2, 6, gl, pos.x, pos.y, fontSize, 100)
            val uvCoordinates = getUV(c)
            val bufferData = floatArrayOf(0f, 1f, 1f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f)
            glObject.initRenderData(this.textureAtlas, bufferData, uvCoordinates)
            cache[c] = glObject
        } else glObject = cache[c]!!

        glObject.draw(pos.x, pos.y, fontSize.getWidth().toFloat(), fontSize.getHeight().toFloat(), 0f, shader)
    }

    fun drawText(text: String, fontSize: Dimension, gl: GL4, pos: PointF, shader: Shader) {
        var x = 0
        var y = 0
        for (c in text.toCharArray()) {
            if (c == '\n') {
                y++
                x = 0
                continue
            }

            drawCharacter(c, fontSize, gl,
                    PointF(pos.x + (fontSize.getWidth() * x++).toFloat(),
                            pos.y + (fontSize.getHeight() * y).toFloat()),
                    shader)
        }
    }

    private fun getUV(c: Char): FloatArray {
        //TODO: wtf refactor this
        val curr = characterCoordinates!![c]!!
        val width = (charSize.getWidth() / textureAtlas!!.width).toFloat()
        val height = (charSize.getHeight() / textureAtlas.height).toFloat()

        return floatArrayOf(
                width * curr.x,
                height * curr.y,
                width * (curr.x + 1),
                height * (curr.y + 1),
                width * curr.x,
                height * (curr.y + 1),
                width * curr.x,
                height * curr.y,
                width * (curr.x + 1),
                height * curr.y,
                width * (curr.x + 1),
                height * (curr.y + 1))
    }

    override fun toString(): String = "Text renderer with " + characterCoordinates!!.size + " characters. \n" + characterCoordinates.toString()

    companion object {
        fun getRenderer(charSize: Dimension,
                        textureFilePath: String,
                        characters: ArrayList<Char>): TextRenderer {
            var textureAtlas: Texture? = null
            var characterCoordinates: HashMap<Char, PointF>? = null

            try {
                textureAtlas = TextureLoader.loadTexture(textureFilePath)
                characterCoordinates = generateMap(charSize, textureAtlas, characters)
            } catch (e: IOException) {
                println("IO error. File can not be read")
                e.printStackTrace()
                textureAtlas = null
            } catch (e: IllegalArgumentException) {
                println("Font texture atlas has wrong format")
                e.printStackTrace()
                characterCoordinates = null
            }

            return TextRenderer(textureAtlas!!, characterCoordinates!!, charSize)
        }

        private fun generateMap(charSize: Dimension,
                                textureAtlas: Texture,
                                characters: ArrayList<Char>): HashMap<Char, PointF> {
            val out = HashMap<Char, PointF>()
            val xStep: Int
            val yStep: Int
            val charsCount: Int

            if (textureAtlas.width % charSize.getWidth() == 0.0 && textureAtlas.height % charSize.getHeight() == 0.0) {
                xStep = (textureAtlas.width / charSize.getWidth()).toInt()
                yStep = (textureAtlas.height / charSize.getHeight()).toInt()
                charsCount = xStep * yStep
                if (charsCount != characters.size)
                    throw IllegalArgumentException("Invalid texture atlas format")
            } else
                throw IllegalArgumentException("Invalid texture atlas format")

            var j = 0
            var k = 0

            for (i in 0 until charsCount) {
                if (charSize.getWidth() * j >= textureAtlas.width) {
                    j = 0
                    k++
                }
                out[characters[i]] = PointF(j++.toFloat(), k.toFloat())
            }

            return out
        }
    }
}