package edu.neu.mhealth.debug;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;
import com.learnopengles.android.common.TextureHelper;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class LessonFourRenderer implements GLSurfaceView.Renderer 
{	
	/** Used for debug logs. */
	private static final String TAG = "LessonFourRenderer";
	
	private final Context mActivityContext;
	
	/**
	 * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
	 * of being located at the center of the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
	 * it positions things relative to our eye.
	 */
	private float[] mViewMatrix = new float[16];

	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	private float[] mProjectionMatrix = new float[16];
	
	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
	private float[] mMVPMatrix = new float[16];
	
	/** 
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	private float[] mLightModelMatrix = new float[16];	
	
	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;
	private final FloatBuffer mCubeTextureCoordinates;
	
	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;
	
	/** This will be used to pass in the modelview matrix. */
	private int mMVMatrixHandle;
	
	/** This will be used to pass in the light position. */
	private int mLightPosHandle;
	
	/** This will be used to pass in the texture. */
	private int mTextureUniformHandle;
	
	/** This will be used to pass in model position information. */
	private int mPositionHandle;
	
	/** This will be used to pass in model color information. */
	private int mColorHandle;
	
	/** This will be used to pass in model normal information. */
	private int mNormalHandle;
	
	/** This will be used to pass in model texture coordinate information. */
	private int mTextureCoordinateHandle;

	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;	
	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;	
	
	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;	
	
	/** Size of the normal data in elements. */
	private final int mNormalDataSize = 3;
	
	/** Size of the texture coordinate data in elements. */
	private final int mTextureCoordinateDataSize = 2;
	
	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	/** This is a handle to our cube shading program. */
	private int mProgramHandle;
		
	/** This is a handle to our light point program. */
	private int mPointProgramHandle;
	
	/** This is a handle to our texture data. */
	private int mTextureDataHandle;
	
	/** This is a handle to our fire texture data. */
	private int mFireTextureDataHandle;
	
	/** This is the instance of Fire */
	private Fire mFire;
	
	/**
	 * Initialize the model data.
	 */
	  public LessonFourRenderer(final Context activityContext)
      {        
              mActivityContext = activityContext;
              mFire = new Fire();
              // Define points for a cube.                
              
              // X, Y, Z
              final float[] cubePositionData =
              {
                              // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle, 
                              // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                              // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                              // usually represent the backside of an object and aren't visible anyways.
                              
                              // Front face
                              -1.0f, 1.0f, 1.0f,                                
                              -1.0f, -1.0f, 1.0f,
                              1.0f, 1.0f, 1.0f, 
                              -1.0f, -1.0f, 1.0f,                                 
                              1.0f, -1.0f, 1.0f,
                              1.0f, 1.0f, 1.0f,
                              
                              // Right face
                              1.0f, 1.0f, 1.0f,                                
                              1.0f, -1.0f, 1.0f,
                              1.0f, 1.0f, -1.0f,
                              1.0f, -1.0f, 1.0f,                                
                              1.0f, -1.0f, -1.0f,
                              1.0f, 1.0f, -1.0f,
                              
                              // Back face
                              1.0f, 1.0f, -1.0f,                                
                              1.0f, -1.0f, -1.0f,
                              -1.0f, 1.0f, -1.0f,
                              1.0f, -1.0f, -1.0f,                                
                              -1.0f, -1.0f, -1.0f,
                              -1.0f, 1.0f, -1.0f,
                              
                              // Left face
                              -1.0f, 1.0f, -1.0f,                                
                              -1.0f, -1.0f, -1.0f,
                              -1.0f, 1.0f, 1.0f, 
                              -1.0f, -1.0f, -1.0f,                                
                              -1.0f, -1.0f, 1.0f, 
                              -1.0f, 1.0f, 1.0f, 
                              
                              // Top face
                              -1.0f, 1.0f, -1.0f,                                
                              -1.0f, 1.0f, 1.0f, 
                              1.0f, 1.0f, -1.0f, 
                              -1.0f, 1.0f, 1.0f,                                 
                              1.0f, 1.0f, 1.0f, 
                              1.0f, 1.0f, -1.0f,
                              
                              // Bottom face
                              1.0f, -1.0f, -1.0f,                                
                              1.0f, -1.0f, 1.0f, 
                              -1.0f, -1.0f, -1.0f,
                              1.0f, -1.0f, 1.0f,                                 
                              -1.0f, -1.0f, 1.0f,
                              -1.0f, -1.0f, -1.0f,
              };        
              
              // R, G, B, A
              final float[] cubeColorData =
              {                                
                              // Front face (red)
                              1.0f, 0.0f, 0.0f, 1.0f,                                
                              1.0f, 0.0f, 0.0f, 1.0f,
                              1.0f, 0.0f, 0.0f, 1.0f,
                              1.0f, 0.0f, 0.0f, 1.0f,                                
                              1.0f, 0.0f, 0.0f, 1.0f,
                              1.0f, 0.0f, 0.0f, 1.0f,
                              
                              // Right face (green)
                              0.0f, 1.0f, 0.0f, 1.0f,                                
                              0.0f, 1.0f, 0.0f, 1.0f,
                              0.0f, 1.0f, 0.0f, 1.0f,
                              0.0f, 1.0f, 0.0f, 1.0f,                                
                              0.0f, 1.0f, 0.0f, 1.0f,
                              0.0f, 1.0f, 0.0f, 1.0f,
                              
                              // Back face (blue)
                              0.0f, 0.0f, 1.0f, 1.0f,                                
                              0.0f, 0.0f, 1.0f, 1.0f,
                              0.0f, 0.0f, 1.0f, 1.0f,
                              0.0f, 0.0f, 1.0f, 1.0f,                                
                              0.0f, 0.0f, 1.0f, 1.0f,
                              0.0f, 0.0f, 1.0f, 1.0f,
                              
                              // Left face (yellow)
                              1.0f, 1.0f, 0.0f, 1.0f,                                
                              1.0f, 1.0f, 0.0f, 1.0f,
                              1.0f, 1.0f, 0.0f, 1.0f,
                              1.0f, 1.0f, 0.0f, 1.0f,                                
                              1.0f, 1.0f, 0.0f, 1.0f,
                              1.0f, 1.0f, 0.0f, 1.0f,
                              
                              // Top face (cyan)
                              0.0f, 1.0f, 1.0f, 1.0f,                                
                              0.0f, 1.0f, 1.0f, 1.0f,
                              0.0f, 1.0f, 1.0f, 1.0f,
                              0.0f, 1.0f, 1.0f, 1.0f,                                
                              0.0f, 1.0f, 1.0f, 1.0f,
                              0.0f, 1.0f, 1.0f, 1.0f,
                              
                              // Bottom face (magenta)
                              1.0f, 0.0f, 1.0f, 1.0f,                                
                              1.0f, 0.0f, 1.0f, 1.0f,
                              1.0f, 0.0f, 1.0f, 1.0f,
                              1.0f, 0.0f, 1.0f, 1.0f,                                
                              1.0f, 0.0f, 1.0f, 1.0f,
                              1.0f, 0.0f, 1.0f, 1.0f
              };
              
              // X, Y, Z
              // The normal is used in light calculations and is a vector which points
              // orthogonal to the plane of the surface. For a cube model, the normals
              // should be orthogonal to the points of each face.
              final float[] cubeNormalData =
              {                                                                                                
                              // Front face
                              0.0f, 0.0f, 1.0f,                                
                              0.0f, 0.0f, 1.0f,
                              0.0f, 0.0f, 1.0f,
                              0.0f, 0.0f, 1.0f,                                
                              0.0f, 0.0f, 1.0f,
                              0.0f, 0.0f, 1.0f,
                              
                              // Right face 
                              1.0f, 0.0f, 0.0f,                                
                              1.0f, 0.0f, 0.0f,
                              1.0f, 0.0f, 0.0f,
                              1.0f, 0.0f, 0.0f,                                
                              1.0f, 0.0f, 0.0f,
                              1.0f, 0.0f, 0.0f,
                              
                              // Back face 
                              0.0f, 0.0f, -1.0f,                                
                              0.0f, 0.0f, -1.0f,
                              0.0f, 0.0f, -1.0f,
                              0.0f, 0.0f, -1.0f,                                
                              0.0f, 0.0f, -1.0f,
                              0.0f, 0.0f, -1.0f,
                              
                              // Left face 
                              -1.0f, 0.0f, 0.0f,                                
                              -1.0f, 0.0f, 0.0f,
                              -1.0f, 0.0f, 0.0f,
                              -1.0f, 0.0f, 0.0f,                                
                              -1.0f, 0.0f, 0.0f,
                              -1.0f, 0.0f, 0.0f,
                              
                              // Top face 
                              0.0f, 1.0f, 0.0f,                        
                              0.0f, 1.0f, 0.0f,
                              0.0f, 1.0f, 0.0f,
                              0.0f, 1.0f, 0.0f,                                
                              0.0f, 1.0f, 0.0f,
                              0.0f, 1.0f, 0.0f,
                              
                              // Bottom face 
                              0.0f, -1.0f, 0.0f,                        
                              0.0f, -1.0f, 0.0f,
                              0.0f, -1.0f, 0.0f,
                              0.0f, -1.0f, 0.0f,                                
                              0.0f, -1.0f, 0.0f,
                              0.0f, -1.0f, 0.0f
              };
              
              // S, T (or X, Y)
              // Texture coordinate data.
              // Because images have a Y axis pointing downward (values increase as you move down the image) while
              // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
              // What's more is that the texture coordinates are the same for every face.
              final float[] cubeTextureCoordinateData =
              {                                                                                                
                              // Front face
                              0.0f, 0.0f,                                 
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f,                                
                              
                              // Right face 
                              0.0f, 0.0f,                                 
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f,        
                              
                              // Back face 
                              0.0f, 0.0f,                                 
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f,        
                              
                              // Left face 
                              0.0f, 0.0f,                                 
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f,        
                              
                              // Top face 
                              0.0f, 0.0f,                                 
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f,        
                              
                              // Bottom face 
                              0.0f, 0.0f,                                 
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f
              };
              
              // Initialize the buffers.
              mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
      .order(ByteOrder.nativeOrder()).asFloatBuffer();                                                        
              mCubePositions.put(cubePositionData).position(0);                
              
              mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
      .order(ByteOrder.nativeOrder()).asFloatBuffer();                                                        
              mCubeColors.put(cubeColorData).position(0);
              
              mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
      .order(ByteOrder.nativeOrder()).asFloatBuffer();                                                        
              mCubeNormals.put(cubeNormalData).position(0);
              
              mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
              .order(ByteOrder.nativeOrder()).asFloatBuffer();
              mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
      }
	
	protected String getVertexShader()
	{
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader);
	}
	
	protected String getFragmentShader()
	{
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader);
	}
	
	// Position the eye in front of the origin.
	float eyeX = 0.0f;
	float eyeY = 0.0f;
	float eyeZ = 0.0f;
	
	// We are looking toward the distance
	float lookX = 0.0f;
	float lookY = 0.0f;
	float lookZ = -5.0f;
	
	// Set our up vector. This is where our head would be pointing were we holding the camera.
	float upX = 0.0f;
	float upY = 1.0f;
	float upZ = 0.0f;
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		// The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
		// Enable texture mapping
		// GLES20.glEnable(GLES20.GL_TEXTURE_2D);
			

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);		

		final String vertexShader = getVertexShader();   		
 		final String fragmentShader = getFragmentShader();			
		
		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		
		
		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
				new String[] {"a_Position",  "a_Color", "a_Normal", "a_TexCoordinate"});								                                							       
        
        // Define a simple shader program for our point.
        final String pointVertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_vertex_shader);        	       
        final String pointFragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_fragment_shader);
        
        final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle, 
        		new String[] {"a_Position"}); 
        
        // Load the texture
//        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.bumpy_bricks_public_domain);
        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.happy_face);
	}	
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;
		
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
		
		mFireTextureDataHandle = mFire.initialize(width, height);
	}	
	
	
	public volatile float globalRotateDegree = 0;
    public float distanceX;
    public float distanceY;
    
	@Override
	public void onDrawFrame(GL10 glUnused) {
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);	
//		
//        upX = (float) (Math.abs(Math.tan(Math.toRadians(globalRotateDegree)))) ;
//        
//        if (globalRotateDegree >= 180 && globalRotateDegree <360) {
//        	upX = - upX;
//        }
//        
//        if (globalRotateDegree >=90 && globalRotateDegree <270) {
//        	upY = -1.0f;
//        } else {
//        	upY = 1.0f;
//        }
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, eyeX, eyeY, lookZ, upX, upY, upZ);	
        
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);
        
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
        
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        mFire.performFrame();
        
        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);        
        
        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
//        if(flip)        
//        	Matrix.translateM(mLightModelMatrix, 0, k, 0.0f, 0.0f);
//        else
//        	Matrix.translateM(mLightModelMatrix, 0, 12-k, 0.0f, 0.0f);
        	
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);                        
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
                     
        // Draw some cubes.        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);        
        drawCube();
                        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);        
        drawCube();
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        drawCube();
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);
        drawCube();
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);        
        drawCube();      
        
        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);        
        drawLight();
	}				
	
	/**
	 * Draws a cube.
	 */			
	private void drawCube()
	{		
		// Pass in the position information
		mCubePositions.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		0, mCubePositions);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		0, mCubeColors);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 
        		0, mCubeNormals);
        
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 
        		0, mCubeTextureCoordinates);
        
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);   
        
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        
        // Pass in the light position in eye space.        
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        
        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);                               
	}	
	
	/**
	 * Draws a point representing the position of the light.
	 */
	private void drawLight()
	{
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  
		
		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}
}

class Fire {

	private static Random random = new Random();
	
	private int textureWidth;
	
	private int textureHeight;
	
	private static final int seedLines = 3;
	
	private byte[] intensityMap;
	
	private ByteBuffer pixelBuffer;
	
	private static final int bytesPerPixel = 3;

	private int[] colors;
	
	private int glFireTextureId = -1;

	private int[] textureCrop = new int[4];	
	
	private boolean glInited = false;
	
	
	private static final int iterationsPerFrame = 1;
	
	private long lastSeedTime = -1L;

	private static final long targetFrameInterval = 1000L / 25L; // target 5 FPS 
	
	private static final long seedInterval = 175L;
	
	private long lastFpsTime = -1L;
	
	private static final long fpsInterval = 1000L * 5;
	
	private int frameCounter = 0;
	
	
	public Fire() {
		generateColors();
	}
	
	private void generateColors() {
		colors = new int[256];
		
		float gg = 200.0f;
		for (int i = 0xff; i >= 0x00; i--) {
			int r = 0;
			r = i << 16;
			
			int g = 0;
			if (gg > 0) {
				g = ((int) gg) << 8;
				if (i < 0xe0) {
					gg -= 2.0f;
				} else {
					gg -= 1.0f;
				}
			}
			
			int b = 0;
			
			colors[i] = (r | g | b);
		}
	}	
	
	public int initialize(int surfaceWidth, int surfaceHeight) {
		// TODO: choose values smarter
		// but remember they have to be powers of 2
//		if (surfaceWidth < surfaceHeight) {
//			textureWidth = 128;
//			textureHeight = 32;
//		} else {
//			textureWidth = 256;
//			textureHeight = 16;
//		}
		textureWidth = 256;
		textureHeight = 256;
		
		textureCrop[0] = 0;
		textureCrop[1] = seedLines;
		textureCrop[2] = textureWidth;
		textureCrop[3] = textureHeight;
		
		// init the intensity map
		intensityMap = new byte[textureWidth * textureHeight];
		
		// init the pixel buffer
		pixelBuffer = ByteBuffer.allocateDirect(textureWidth * textureHeight * bytesPerPixel);
		
		// init the GL settings
//		if (glInited) {
//			resetGl(gl);
//		}
//		initGl(gl, surfaceWidth, surfaceHeight);
		
		// init the GL texture
		return initFireTexture();
	}	
	
//	private void resetGl(GL11 gl) {
//        gl.glMatrixMode(GL10.GL_PROJECTION);
//        gl.glPopMatrix();
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glPopMatrix();				
//	}
	
//	private void initGl(GL11 gl, int surfaceWidth, int surfaceHeight) {
//        gl.glShadeModel(GL11.GL_FLAT);
//        gl.glFrontFace(GL11.GL_CCW);
//        
//        gl.glEnable(GL11.GL_TEXTURE_2D);
//
//        gl.glMatrixMode(GL11.GL_PROJECTION);
//        gl.glPushMatrix();
//        gl.glLoadIdentity();
//        gl.glOrthof(0.0f, surfaceWidth, 0.0f, surfaceHeight, 0.0f, 1.0f);
//
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glPushMatrix();
//        gl.glLoadIdentity();
//        
//        glInited = false;
//	}
//	
	private static int nextRandomInt(int max) {
		return max == 0 ? 0 : random.nextInt(max);
	}
	
	private void seedIntensity() {
		int y = intensityMap.length - textureWidth;
		
		for (int line = 0; line < seedLines; ++line) {
			boolean on = random.nextBoolean();
			for (int x = 0; x < textureWidth; ++x) {
				// magic settings for good looking fire				
				if ((on && nextRandomInt(20 / (line + 1)) == 0) 
						|| ((! on) && nextRandomInt(9) == 0)) {
					
					on = ! on;
				}				

				intensityMap[y + x] = (byte) (on ? 0xff : 0x00);
			}
			y -= textureWidth;
		}
	}
	
	private void iterateIntensity() {
		int y = intensityMap.length - textureWidth * 2;
		
		int v1, v2, v3, v4;
		while (y > 0) {
			for (int x = 0; x < textureWidth; ++x) {
				// now take the current value, the values from both sides
				// and the one from the bottom				
				v1 = unsignedByte(intensityMap[y + x]);
				if (x < textureWidth - 1) {
					v2 = unsignedByte(intensityMap[y + x + 1]);
				} else {
					v2 = 0;
				}
				if (x > 0) {
					v3 = unsignedByte(intensityMap[y + x - 1]);
				} else {
					v3 = 0;
				}
				v4 = unsignedByte(intensityMap[y + textureWidth + x]);
				
				int v = (v1 + v2 + v3 + v4) / (((x != 0) && (x != textureWidth - 1)) ? 4 : 3);
				
				// magic values - needed for good decay
				v = v - 2 * (255 - v) / 128;
				
				// now clip the value
				if (v > 0xff) {
					v = 0xff;
				} else if (v < 0x00) {
					v = 0;
				}
				
				intensityMap[y + x] = (byte) v;
			}
			
			y -= textureWidth;
		}
	}
	
	private static int unsignedByte(byte v) {
		return (v >= 0 ? v : (256 + v));
	}
	
	private void updatePixelsFromIntensity() {
		pixelBuffer.rewind();
		// we need to output the pixels upside down due to glDrawTex peculiarities
		for (int y = intensityMap.length - textureWidth; y > 0; y -= textureWidth) {
			for (int x = 0; x < textureWidth; ++x) {
				int pixel = pixelFromIntensity(intensityMap[y + x]);
				pixelBuffer.put((byte) (pixel >> 16));
				pixelBuffer.put((byte) ((pixel >> 8) & 0xff));
				pixelBuffer.put((byte) (pixel & 0xff));
			}
		}
	}
	
	private int pixelFromIntensity(byte intensity) {
		return colors[unsignedByte(intensity)];
	}

	private void releaseTexture() {
		if (glFireTextureId != -1) {
			GLES20.glDeleteTextures(1, new int[] { glFireTextureId }, 0);
		}		
	}
	
	private int initFireTexture() {
		releaseTexture();
		int[] fireTextureHandle = new int[1];
		GLES20.glGenTextures(1, fireTextureHandle, 0);
		glFireTextureId = fireTextureHandle[0];
		if (fireTextureHandle[0] != 0) {
			// we want to modify this texture so bind it
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glFireTextureId);

			// GL_LINEAR gives us smoothing since the texture is larger than the screen
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			// repeat the edge pixels if a surface is larger than the texture
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);  
        
			// now, let's init the texture with pixel values
			updatePixelsFromIntensity();
        
			// and init the GL texture with the pixels
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth, textureHeight,
				0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);        
			// at this point, we are OK to further modify the texture
			// using glTexSubImage2D
			return glFireTextureId;
		}
		return 0;
	}
	
	public void dispose(GL11 gl) {
		releaseTexture();
	}
	
	public void performFrame() {
		Log.d("czxopengl", "performFrame");
		long frameStartTime = System.currentTimeMillis();
		if (lastSeedTime == -1L || (frameStartTime - lastSeedTime) >= seedInterval) {
			seedIntensity();
			lastSeedTime = frameStartTime;
		}
		for (int i = 0; i < iterationsPerFrame; ++i) {
			iterateIntensity();
		}
		updatePixelsFromIntensity();
//		// Clear the surface
//		gl.glClearColorx(0, 0, 0, 0);
//		gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
//		
		// Choose the texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glFireTextureId);
		// Update the texture
		GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, textureWidth, textureHeight, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		
		// Draw the texture on the surface
//		GLES20.glTexParameteriv(GLES20.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, textureCrop, 0);
//		((GL11Ext) gl).glDrawTexiOES(0, 0, 0, surfaceWidth, surfaceHeight);
		
		// Sleep the extra time
//		long frameEndTime = System.currentTimeMillis();
//		long delta = frameEndTime - frameStartTime;
//		if (targetFrameInterval - delta > 10L) {
//			try {
//				Thread.sleep(targetFrameInterval - delta);
//			} catch (InterruptedException e) {}
//		}
		
		// Output FPS if necessary
//		frameCounter++;
//		if (lastFpsTime == -1L) {
//			lastFpsTime = frameEndTime;
//		} else if ((frameEndTime - lastFpsTime) >= fpsInterval) {
//			float fps = frameCounter / ((frameEndTime - lastFpsTime) / 1000.0f);
//			Log.d("FPS", String.format("%1.0f", fps));
//			
//			frameCounter = 0;
//			lastFpsTime = frameEndTime;
//		}
	}
}
