import com.jogamp.opengl.GL4bc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Camera {
  // This class is a c++ port of this, I based it off this
  // https://learnopengl.com/Getting-started/Camera
  // I hope this is not an academic integrity violation

  private Vector3f position;
  private Vector3f cameraFront;
  private Vector3f up;

  private Vector3f positionPlusFront;
  private Vector3f right;

  private float yaw;
  private float pitch;

  private float speed;
  private float sensitivity;
  private float fov;
  private float zNear;
  private float zFar;

  private Matrix4f projection;
  private Matrix4f view;

  public Camera(GL4bc gl) {
    this.position = new Vector3f(0, 0, -10);
    this.cameraFront = new Vector3f(0, 0, -1);
    this.up = new Vector3f(0, 1, 0);
    this.right = new Vector3f(0, 0, 0);
    this.positionPlusFront = new Vector3f(position);
    this.yaw = -90;
    this.pitch = 0;
    this.speed = 0.1f;
    this.sensitivity = 0.1f;
    this.fov = 45;
    this.zNear = 0.1f;
    this.zFar = 1000;
    this.projection = new Matrix4f();
    this.view = new Matrix4f();
  }

  public float getYaw() {
    return yaw;
  }

  public float getPitch() {
    return pitch;
  }

  public void processKeyboard(Movement direction, boolean isPressed) {
    float velocity = speed;
    if (isPressed) {
      Vector3f tmp = new Vector3f();
      switch (direction) {
        case FORWARD -> position = position.add(cameraFront.mul(velocity));
        case BACKWARD -> position = position.sub(cameraFront.mul(velocity));
        case LEFT -> position.sub(cameraFront.cross(up, tmp).mul(speed)).normalize();
        case RIGHT -> position.add(cameraFront.cross(up, tmp).mul(speed));
      }
      position.y = 0;
      System.out.println(position);
    }
    updateCameraVectors();
  }

  public Vector3f getCameraFront() {
    return cameraFront;
  }

  private Matrix4f createLookAtMatrix(Vector3f position, Vector3f target, Vector3f up) {
    return new Matrix4f().lookAt(position, target, up);
  }

  public void setPosition(Vector3f position) {
    this.position = position;
  }

  public void setCameraFront(Vector3f cameraFront) {
    this.cameraFront = cameraFront;
  }

  public void reset(Movement direction) {
    if (direction == Movement.RESET) {
      this.position = new Vector3f(0, 0, -5);
      this.cameraFront = new Vector3f(0, 0, -1);
      this.up = new Vector3f(0, 1, 0);
      this.positionPlusFront = positionPlusFront.add(cameraFront).add(position);
      this.yaw = -90;
      this.pitch = 0;
      this.updateCameraVectors();
    }
  }

  public void setUp(Vector3f up) {
    this.up = up;
  }

  public void processMouseMovement(float xoffset, float yoffset) {
    yaw += xoffset * sensitivity;
    pitch += yoffset * sensitivity;

    if (pitch > 89) {
      pitch = 89;
    } else if (pitch < -89) {
      pitch = -89;
    }

    updateCameraVectors();
  }

  public Matrix4f getViewMatrix() {
    view.identity();
    return view.lookAt(position, cameraFront, up);
  }

  public Vector3f getPosition() {
    return position;
  }

  public Vector3f getPositionPlusFront() {
    return positionPlusFront;
  }

  public Vector3f getUp() {
    return up;
  }

  private void updateCameraVectors() {
    Vector3f front = new Vector3f();

    front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
    front.y = (float) Math.sin(Math.toRadians(pitch));
    front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));

    front.normalize();
    this.cameraFront = front;
    this.right = front.cross(this.up, right);
    this.right.normalize();
    up = right.cross(front, up);
    this.up.normalize();


    Vector3f tmp = new Vector3f();
    System.out.println("Front: " +cameraFront);
    positionPlusFront.add(cameraFront, tmp);
    positionPlusFront.add(position, tmp);
    System.out.println("Position: " +position);
    System.out.println("Pos+Front: " +positionPlusFront);
  }

  public Vector3fc getLookAt() {
    return position.add(cameraFront);
  }

  public enum Movement {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    RESET
  }
}
