import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.TileRendererBase;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import jogamp.opengl.glu.nurbs.CArrayOfArcs;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.NumberFormat;

import static com.jogamp.opengl.GL.*;

public class OBJRenderer implements GLEventListener, MouseMotionListener, KeyListener {

    private TextRenderer textRenderer;

    private int width;
    private int height;
    private boolean verbose = true;
    private ShaderState st;
    private float deltaTime = 0.0f;
    private String[] cubeMapFiles = {
            "textures/skybox/right.jpg",
            "textures/skybox/left.jpg",
            "textures/skybox/top.jpg",
            "textures/skybox/bottom.jpg",
            "textures/skybox/front.jpg",
            "textures/skybox/back.jpg"
    };
    private TileRendererBase tileRendererInUse = null;

    public OBJRenderer() {
    }


    int vertShaderID;
    int fragShaderID;


    private int shaderProgramID;

    Camera camera;

    private int[] vbo_handle = new int[1];

    private int[] vao_handle = new int[1];
    private int[] ebo_handle = new int[1];
    Texture cube;

    @Override
    public void init(final GLAutoDrawable glad) {
        final GL4bc gl = glad.getGL().getGL4bc();
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        camera = new Camera(gl);
        width = glad.getSurfaceWidth();
        height = glad.getSurfaceHeight();
        gl.glEnable(GL_TEXTURE_2D);
        gl.glEnable(GL_DEPTH_TEST);
        initShaders(gl);

        try {
            cube = loadTexture(gl, "textures/block.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        initBuffers(gl);
        // Convert to MBs
        double texMem = cube.getEstimatedMemorySize() / (1024.0 * 1024);
        System.out.println("Tex Mem Size: " + texMem + " MB");
        gl.glUseProgram(shaderProgramID);

        gl.glUniform1i(gl.glGetUniformLocation(shaderProgramID, "tex"), 0);


    }

    private Texture loadTexture(GL4bc gl, String fileName) throws IOException {
        Texture texture = TextureIO.newTexture(new File(fileName), true);
        gl.glActiveTexture(gl.GL_TEXTURE0);
        texture.bind(gl);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
        // set texture filtering parameters
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
        return texture;
    }

    private Vector3f[] cubePositions = {
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(2.0f, 5.0f, -15.0f),
            new Vector3f(-1.5f, -2.2f, -2.5f),
            new Vector3f(-3.8f, -2.0f, -12.3f),
            new Vector3f(2.4f, -0.4f, -3.5f),
            new Vector3f(-1.7f, 3.0f, -7.5f),
            new Vector3f(1.3f, -2.0f, -2.5f),
            new Vector3f(1.5f, 2.0f, -2.5f),
            new Vector3f(1.5f, 0.2f, -1.5f),
            new Vector3f(-1.3f, 1.0f, -1.5f)
    };


    private void initShaders(final GL4bc gl) {
        final ShaderCode vp = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(),
                "shader", "shader/bin", "triangle", true);
        final ShaderCode fp = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(),
                "shader", "shader/bin", "triangle", true);
        vp.defaultShaderCustomization(gl, true, true);
        fp.defaultShaderCustomization(gl, true, true);
        final ShaderProgram program = new ShaderProgram();
        program.init(gl);
        program.add(vp);
        program.add(fp);
        System.out.println("GLSL version: " + gl.glGetString(GL4bc.GL_SHADING_LANGUAGE_VERSION));
        if (!program.link(gl, System.out)) {
            System.err.println("Could not link program: ");
        }
        if (gl.glGetError() != GL4bc.GL_NO_ERROR) {
            System.err.println("Error: " + gl.glGetError());
        }
        shaderProgramID = program.program();
        vertShaderID = vp.id();
        fragShaderID = fp.id();
    }


    float[] texArr = {
            // positions      // colors          // texture coords
            0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top right
            0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom left
            -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f  // top left
    };


    int[] indices = {  // note that we start from 0!
            0, 1, 3,   // first triangle
            1, 2, 3    // second triangle
    };

    private void initBuffers(final GL4bc gl) {
        gl.glGenVertexArrays(1, vao_handle, 0);
        gl.glBindVertexArray(vao_handle[0]);

        // Create a new Vertex Buffer Object in memory and select it (bind) this is for the square of triangles
        gl.glGenBuffers(1, IntBuffer.wrap(vbo_handle));
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo_handle[0]);
        gl.glBufferData(GL_ARRAY_BUFFER, texArr.length * 4, FloatBuffer.wrap(texArr), GL_STATIC_DRAW);

        gl.glGenBuffers(1, IntBuffer.wrap(vbo_handle));
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo_handle[0]);
        gl.glBufferData(GL_ARRAY_BUFFER, Cube.verticesCube.length * 4, FloatBuffer.wrap(Cube.verticesCube), GL_STATIC_DRAW);


        //  gl.glGenBuffers(1, IntBuffer.wrap(ebo_handle));
        //gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo_handle[0]);
        //gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, IntBuffer.wrap(indices), GL_STATIC_DRAW);

      //  gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
       // gl.glEnableVertexAttribArray(0);
       // gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4);
        //gl.glEnableVertexAttribArray(1);
        //gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4);
        //gl.glEnableVertexAttribArray(2);


        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Buffers.SIZEOF_FLOAT, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Buffers.SIZEOF_FLOAT, 3 * Buffers.SIZEOF_FLOAT);
        gl.glEnableVertexAttribArray(1);
    }


    /*
    Inits the VAO and VBO buffers we need to draw with "core" mode OpenGL
     */
    @Override
    public void display(final GLAutoDrawable glad) {
        final GL4bc gl = glad.getGL().getGL4bc();
        gl.glUseProgram(shaderProgramID);
        int width = glad.getSurfaceWidth();
        int height = glad.getSurfaceHeight();
        float time = (float) System.currentTimeMillis();
        gl.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
        Matrix4f view = new Matrix4f();
        float time2 = glad.getAnimator().getFPSStartTime();
        FloatBuffer matrixBuffer2 = Buffers.newDirectFloatBuffer(16);
        float radius = 10.0f;

       // view.lookAt(camX, 0, camZ, 0, 0, 0, 0, 1, 0).get(matrixBuffer2);

        float randX = (float) (Math.random() * radius);

        view = camera.getViewMatrix();
        view.get(matrixBuffer2);
        Matrix4f projection = new Matrix4f();
        FloatBuffer matrixBuffer1 = Buffers.newDirectFloatBuffer(16);
        projection.perspective((float) Math.toRadians(90), (float) width / (float) height, 0.1f, 100.0f).get(matrixBuffer1);

        FloatBuffer matrixBuffer = Buffers.newDirectFloatBuffer(16);
        Matrix4f model = new Matrix4f();
        model.identity();
        model.translate(0,0,-10);
        model.get(matrixBuffer);
        gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgramID, "model"), 1, false, matrixBuffer);
        gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgramID, "view"), 1, false, matrixBuffer2);
        gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgramID, "projection"), 1, false, matrixBuffer1);

        gl.glActiveTexture(GL_TEXTURE0);
        cube.bind(gl);
        int uniform_texture = gl.glGetUniformLocation(shaderProgramID, "tex");
        gl.glUniform1i(uniform_texture, 0);
        gl.glBindVertexArray(vao_handle[0]);


        gl.glDrawArrays(GL_TRIANGLES, 0, 36);


        // gl.glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);
        gl.glUseProgram(0);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(5);

        String cameraPos = nf.format(camera.getPosition().x) + " " + nf.format(camera.getPosition().y) + " " + nf.format(camera.getPosition().z);
        String cameraRot = nf.format(camera.getYaw()) + " " + nf.format(camera.getPitch()) + "";

        textRenderer.beginRendering(glad.getSurfaceWidth(), glad.getSurfaceHeight());
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        textRenderer.draw("RandX: " + nf.format(randX), 10, glad.getSurfaceHeight() - 20);

        textRenderer.draw("Camera Position: " + cameraPos, 10, glad.getSurfaceHeight() - 40);
        textRenderer.draw("Camera Rotation: " + cameraRot, 10, glad.getSurfaceHeight() - 60);
        textRenderer.endRendering();

    }


    @Override
    public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width,
                        final int height) {
        GL4 gl = glad.getGL().getGL4();
        Matrix4f view = new Matrix4f();
        FloatBuffer matrixBuffer2 = Buffers.newDirectFloatBuffer(16);
        view.lookAt(0.f,0.f,0.f,0.f,0.f,0.f,0.f,1.f,0.f).get(matrixBuffer2);
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(90), (float) width / (float) height, 0.1f, 100.0f);
        FloatBuffer matrixBuffer = Buffers.newDirectFloatBuffer(16);
        projection.get(matrixBuffer);
        gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgramID, "projection"), 1, false, matrixBuffer);
        gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgramID, "view"), 1, false, matrixBuffer2);
        gl.glViewport(0, 0, width, height);
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

        gl.glDetachShader(shaderProgramID, vertShaderID);
        gl.glDetachShader(shaderProgramID, fragShaderID);
        gl.glDeleteProgram(shaderProgramID);
        System.out.println("Deleted shader program " + shaderProgramID);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> camera.processKeyboard(Camera.Movement.FORWARD,true);
            case KeyEvent.VK_S -> camera.processKeyboard(Camera.Movement.BACKWARD,true);
            case KeyEvent.VK_A -> camera.processKeyboard(Camera.Movement.LEFT,true);
            case KeyEvent.VK_D -> camera.processKeyboard(Camera.Movement.RIGHT,true);
            case KeyEvent.VK_R -> camera.reset(Camera.Movement.RESET);
            case KeyEvent.VK_L -> lock = !lock;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> camera.processKeyboard(Camera.Movement.FORWARD,false);
            case KeyEvent.VK_S -> camera.processKeyboard(Camera.Movement.BACKWARD,false);
            case KeyEvent.VK_A -> camera.processKeyboard(Camera.Movement.LEFT,false);
            case KeyEvent.VK_D -> camera.processKeyboard(Camera.Movement.RIGHT,false);
            case KeyEvent.VK_R -> camera.reset(Camera.Movement.RESET);
        }
    }

    float lastX;
    boolean lock;
    float lastY;
    float xoffset;
    float yoffset;

    @Override
    public void mouseDragged(MouseEvent e) {
        float xpos =e.getX();
        float ypos= e.getY();
        Robot r;
        try {
            r = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }

        if(lock == true){
            r.mouseMove(width/2,height/2);
        }

        float xoffset = xpos - lastX;
        float yoffset = lastY - ypos; // reversed since y-coordinates range from bottom to top
        lastX = xpos;
        lastY = ypos;

        boolean firstMouse = true;
        if(firstMouse){
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
        }

        camera.processMouseMovement(xoffset, yoffset);


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

