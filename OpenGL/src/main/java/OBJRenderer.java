import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.TileRendererBase;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class OBJRenderer implements GLEventListener, MouseMotionListener, KeyListener {
    private ShaderState st;
    private ShaderState cubeMapSt;
    private PMVMatrix pmvMatrix;
    private GLUniformData pmvMatrixUniform;
    private GLUniformData timeUni;
    private GLUniformData cubeMapUniform;
    private GLArrayDataServer vertices;
    private GLArrayDataServer skyBoxVertices;
    private TextRenderer textRenderer;

    private long millisOffset;
    FloatBuffer OBJvertices;
    FloatBuffer texCoords;
    FloatBuffer normals;
    IntBuffer indices;

    Obj obj;
    private long t0;
    private int swapInterval = 0;
    private float aspect = 1.0f;
    private boolean doRotate = true;
    private boolean verbose = true;
    private boolean clearBuffers = false;
    private float deltaTime = 0.0f;
    private float time = 0.0f;

    private String[] cubeMapFiles = {
            "tex/right.jpg",
            "tex/left.jpg",
            "tex/top.jpg",
            "tex/bottom.jpg",
            "tex/front.jpg",
            "tex/back.jpg"
    };
    private TileRendererBase tileRendererInUse = null;
    private boolean doRotateBeforePrinting;
    boolean[] move = new boolean[6];

    public OBJRenderer() {
        this.swapInterval = 60;
    }

    FloatBuffer timeBuffer;

    boolean forward, backward, left, right, reset;

    public void setAspect(final float aspect) {
        this.aspect = aspect;
    }

    float mouseX = 0.0f;
    float mouseY = 0.0f;
    Camera camera;

    @Override
    public void init(final GLAutoDrawable glad) {
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        try {
            InputStream is = new FileInputStream("./models/cube.obj");
            obj = ObjUtils.convertToRenderable(
                    ObjReader.read(is));
        } catch (IOException e) {
            e.printStackTrace();
        }

        indices = ObjData.getFaceVertexIndices(obj);
        texCoords = ObjData.getTexCoords(obj, 2);
        OBJvertices = ObjData.getVertices(obj);

        if (verbose) {
            System.err.println(Thread.currentThread() + " RedSquareES2.init: tileRendererInUse "
                    + tileRendererInUse);
        }
        final GL2ES2 gl = glad.getGL().getGL2ES2();
        int cubeMapTex = loadCubeMap(gl, cubeMapFiles);

        if (verbose) {
            System.err.println("RedSquareES2 init on " + Thread.currentThread());
            System.err.println("Chosen GLCapabilities: " + glad.getChosenGLCapabilities());
            System.err.println("INIT GL IS: " + gl.getClass().getName());
            System.err.println(JoglVersion.getGLStrings(gl, null, false).toString());
        }
        if (!gl.hasGLSL()) {
            System.err.println("No GLSL available, no rendering.");
            return;
        }
        st = new ShaderState();
        cubeMapSt = new ShaderState();
        cubeMapSt.setVerbose(true);
        st.setVerbose(true);

        final ShaderCode cubeMapVP = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(),
                "shader", "shader/bin", "cubemap", true);
        final ShaderCode cubeMapFP = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(),
                "shader", "shader/bin", "cubemap", true);
        final ShaderCode vp0 = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(),
                "shader", "shader/bin", "RedSquareShader", true);
        final ShaderCode fp0 = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(),
                "shader", "shader/bin", "RedSquareShader", true);


        vp0.defaultShaderCustomization(gl, true, true);
        fp0.defaultShaderCustomization(gl, true, true);
        cubeMapVP.defaultShaderCustomization(gl, true, true);
        cubeMapFP.defaultShaderCustomization(gl, true, true);

        final ShaderProgram sp0 = new ShaderProgram();
        sp0.add(gl, vp0, System.err);
        sp0.add(gl, fp0, System.err);
       // sp0.add(gl, cubeMapVP, System.err);
       // sp0.add(gl, cubeMapFP, System.err);

        st.attachShaderProgram(gl, sp0, true);
        cubeMapSt.attachShaderProgram(gl, sp0, true);
        // setup mgl_PMVMatrix
        pmvMatrix = new PMVMatrix();
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        pmvMatrix.glLoadIdentity();
        gl.glClearColor(57,57, 57, 255);

        timeBuffer = Buffers.newDirectFloatBuffer(1);
        pmvMatrixUniform = new GLUniformData("mgl_PMVMatrix", 4, 4, pmvMatrix.glGetPMvMatrixf()); // P,
        timeUni = new GLUniformData("iGlobalTime", 0.0f);



        st.ownUniform(pmvMatrixUniform);
        st.uniform(gl, pmvMatrixUniform);


    //    cubeMapSt.ownUniform(pmvMatrixUniform);
    //    cubeMapSt.uniform(gl, pmvMatrixUniform);
        //   st.ownUniform(timeUni);
        gl.glDepthMask(false);
/*
        skyBoxVertices = GLArrayDataServer.createGLSL("aPos", 3, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
        FloatBuffer skyBoxVerticesBuffer = Buffers.newDirectFloatBuffer(Cube.skyboxVertices);
        skyBoxVertices.put(skyBoxVerticesBuffer);
        skyBoxVertices.seal(gl,true);
        skyBoxVertices.enableBuffer(gl,false);

 */
        // Allocate Vertex Array
        vertices = GLArrayDataServer.createGLSL("mgl_Vertex", 3, GL.GL_FLOAT, false, 4,
                GL.GL_STATIC_DRAW);
        vertices.put(OBJvertices);
        vertices.seal(gl, true);
        st.ownAttribute(vertices, true);
        vertices.enableBuffer(gl, false);



        // OpenGL Render Settings
        gl.glEnable(GL.GL_DEPTH_TEST);
        st.useProgram(gl, false);
        t0 = System.currentTimeMillis();
        if (verbose) {
            System.err.println(Thread.currentThread() + " RedSquareES2.init FIN");
        }

        camera = new Camera(gl, pmvMatrix);
        millisOffset = System.currentTimeMillis();
    }

    @Override
    public void display(final GLAutoDrawable glad) {


        final long t1 = System.currentTimeMillis();

        deltaTime = time - t1;
        time = System.currentTimeMillis();
        final GL2ES2 gl = glad.getGL().getGL2ES2();
        int width = glad.getSurfaceWidth();
        int height = glad.getSurfaceHeight();

        timeUni.setData((System.currentTimeMillis() - millisOffset) / 1000.0f);
        // st.uniform(gl, timeUni);
        //System.out.println("Render Matrix: "+ pmvMatrix.toString());
        if (clearBuffers) {
            if (null != tileRendererInUse) {
                gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
            } else {
                gl.glClearColor(0, 0, 0, 0);
            }
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        }
        if (!gl.hasGLSL()) {
            return;
        }
        st.useProgram(gl, true);
        // One rotation every four seconds
        textRenderer.setColor(Color.white);
        textRenderer.beginRendering(width, height, true);
        textRenderer.draw("Camera Position: " + Arrays.toString(camera.getPosition()), 10, height - 20);
        textRenderer.draw("Camera Direction: " + Arrays.toString(camera.getFront()), 10, height - 40);
        textRenderer.endRendering();
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.glTranslatef(0, 0, -10);
        System.out.println("Camera Position: " + Arrays.toString(camera.getPosition()));
        System.out.println("Camera Direction: " + Arrays.toString(camera.getFront()));

        if (doRotate) {
            final float ang = ((t1 - t0) * 360.0F) / 4000.0F;
            pmvMatrix.glRotatef(ang, 0, 0, 1);
            pmvMatrix.glRotatef(ang, 0, 1, 0);
        }

       // skyBoxVertices.enableBuffer(gl, true);
      //  gl.glDrawArrays(GL.GL_TRIANGLES, 0, 36);
       // skyBoxVertices.enableBuffer(gl, false);

        st.uniform(gl, pmvMatrixUniform);

        float rand = (float) Math.random()*0.5f;

        camera.position[0] = rand;

        //gl.glClearColor(57,57, 255, 255);
        //gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Draw a square
        vertices.enableBuffer(gl, true);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, indices.capacity());
        vertices.enableBuffer(gl, false);

        vertices.enableBuffer(gl, true);
        pmvMatrix.glTranslatef(-5, 0, -100);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, indices.capacity());
        vertices.enableBuffer(gl, false);
        st.useProgram(gl, false);


    }

    @Override
    public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width,
                        final int height) {
        //final GL2ES2 gl = glad.getGL().getGL2ES2();
        //gl.setSwapInterval(swapInterval);
       // reshapeImpl(gl, x, y, width, height, width, height);
    }


    void reshapeImpl(final GL2ES2 gl, final int tileX, final int tileY, final int tileWidth,
                     final int tileHeight, final int imageWidth, final int imageHeight) {
        if (verbose) {
            System.err.println(Thread.currentThread() + " RedSquareES2.reshape " + tileX + "/"
                    + tileY + " " + tileWidth + "x" + tileHeight + " of " + imageWidth + "x"
                    + imageHeight + ", swapInterval " + swapInterval + ", drawable 0x"
                    + Long.toHexString(gl.getContext().getGLDrawable().getHandle())
                    + ", tileRendererInUse " + tileRendererInUse);
        }
        // Thread.dumpStack();
        if (!gl.hasGLSL()) {
            return;
        }

        st.useProgram(gl, true);
        // Set location in front of camera
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        pmvMatrix.glLoadIdentity();

        // compute projection parameters 'normal' perspective
        final float fovy = camera.zoom;
        final float aspect2 = ((float) imageWidth / (float) imageHeight) / aspect;
        final float zNear = 1.0f;
        final float zFar = 1000.0f;

        // compute projection parameters 'normal' frustum
        final float top = (float) Math.tan(fovy * ((float) Math.PI) / 360.0f) * zNear;
        final float bottom = -1.0f * top;
        final float left = aspect2 * bottom;
        final float right = aspect2 * top;
        final float w = right - left;
        final float h = top - bottom;

        // compute projection parameters 'tiled'
        final float l = left + tileX * w / imageWidth;
        final float r = l + tileWidth * w / imageWidth;
        final float b = bottom + tileY * h / imageHeight;
        final float t = b + tileHeight * h / imageHeight;

        //pmvMatrix.glFrustumf(l, r, b, t, zNear, zFar);
       //  pmvMatrix.glOrthof(-4.0f, 4.0f, -4.0f, 4.0f, 1.0f, 100.0f);
        st.uniform(gl, pmvMatrixUniform);
        st.useProgram(gl, false);

        System.err.println(Thread.currentThread() + " RedSquareES2.reshape FIN");
        System.out.println(doRotate);
    }

    @Override
    public void dispose(final GLAutoDrawable glad) {
        if (verbose) {
            System.err.println(Thread.currentThread() + " RedSquareES2.dispose: tileRendererInUse "
                    + tileRendererInUse);
        }
        final GL2ES2 gl = glad.getGL().getGL2ES2();
        if (!gl.hasGLSL()) {
            return;
        }
        st.destroy(gl);
        st = null;
        pmvMatrix = null;
        if (verbose) {
            System.err.println(Thread.currentThread() + " RedSquareES2.dispose FIN");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
      if(e.getKeyChar() == 'w') {
          camera.processKeyboard(Camera.Movement.FORWARD, deltaTime);
      }
      if(e.getKeyChar() == 's') {
          camera.processKeyboard(Camera.Movement.BACKWARD, deltaTime);
      }
      if(e.getKeyChar() == 'a') {
          camera.processKeyboard(Camera.Movement.LEFT, deltaTime);
      }
      if(e.getKeyChar() == 'd') {
          camera.processKeyboard(Camera.Movement.RIGHT, deltaTime);
      }

    }

    @Override
    public void keyPressed(KeyEvent e) {
       switch (e.getKeyCode()) {
           case KeyEvent.VK_W -> camera.processKeyboard(Camera.Movement.FORWARD, deltaTime);
           case KeyEvent.VK_S -> camera.processKeyboard(Camera.Movement.BACKWARD, deltaTime);
           case KeyEvent.VK_A -> camera.processKeyboard(Camera.Movement.LEFT, deltaTime);
           case KeyEvent.VK_D -> camera.processKeyboard(Camera.Movement.RIGHT, deltaTime);
       }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        char key = e.getKeyChar();
        if (key == 'w') {
            move[0] = false;
        }
        if (key == 's') {
            move[1] = false;
        }
        if (key == 'a') {
            move[2] = false;
        }
        if (key == 'd') {
            move[3] = false;
        }
        if (key == 'r') {
            move[4] = false;
        }
        if (key == 'v') {
            move[5] = false;
        }
        if (key == 'x') {
            doRotate = !doRotate;
        }
    }

    float lastX;
    float lastY;
    @Override
    public void mouseDragged(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
       camera.processMouseMovement(e.getX() - lastX, lastY - e.getY(),true);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }


    int loadCubeMap(GL2ES2 gl, String[] filenames) {
        int[] textureId = new int[1];
        gl.glGenTextures(1, textureId, 0);
        gl.glBindTexture(GL2ES2.GL_TEXTURE_CUBE_MAP, textureId[0]);
        int width, height, nrChannels;
        for (int i = 0; i < filenames.length; i++) {
            String filename = filenames[i];
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (image == null) {
                System.err.println("Failed to load image: " + filename);
                return 0;
            }
            width = image.getWidth();
            height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
        }
        gl.glTexParameteri(GL2ES2.GL_TEXTURE_CUBE_MAP, GL2ES2.GL_TEXTURE_MAG_FILTER, GL2ES2.GL_LINEAR);
        gl.glTexParameteri(GL2ES2.GL_TEXTURE_CUBE_MAP, GL2ES2.GL_TEXTURE_MIN_FILTER, GL2ES2.GL_LINEAR);
        gl.glTexParameteri(GL2ES2.GL_TEXTURE_CUBE_MAP, GL2ES2.GL_TEXTURE_WRAP_S, GL2ES2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2ES2.GL_TEXTURE_CUBE_MAP, GL2ES2.GL_TEXTURE_WRAP_T, GL2ES2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2ES2.GL_TEXTURE_CUBE_MAP, GL2ES2.GL_TEXTURE_WRAP_R, GL2ES2.GL_CLAMP_TO_EDGE);
        return textureId[0];
    }
}

