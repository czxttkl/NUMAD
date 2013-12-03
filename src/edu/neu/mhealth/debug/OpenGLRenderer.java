package edu.neu.mhealth.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Scanner;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;
import com.learnopengles.android.common.TextureHelper;

import edu.neu.mhealth.debug.opengl.OpenGLBug;
import edu.neu.mhealth.debug.opengl.OpenGLBugManager;
import edu.neu.mhealth.debug.opengl.OpenGLFire;

/**
 * This class implements our custom renderer. Note that the GL10 parameter
 * passed in is unused for OpenGL ES 2.0 renderers -- the static class GLES20 is
 * used instead.
 */
public class OpenGLRenderer implements GLSurfaceView.Renderer {
	/** Used for debug logs. */
	private static final String TAG = "mDebug";

	private final Context mActivityContext;
	private final MainActivity mCameraActivityInstance;

	/** The default scale ratio for 3d model */
	public final static float SCALE_RATIO = 12.0f;

	/**
	 * Store the model matrix. This matrix is used to move models from object
	 * space (where each model can be thought of being located at the center of
	 * the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix
	 * transforms world space to eye space; it positions things relative to our
	 * eye.
	 */
	private float[] mViewMatrix = new float[16];

	/**
	 * Store the projection matrix. This is used to project the scene onto a 2D
	 * viewport.
	 */
	private float[] mProjectionMatrix = new float[16];

	/**
	 * Allocate storage for the final combined matrix. This will be passed into
	 * the shader program.
	 */
	private float[] mMVPMatrix = new float[16];

	/**
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	private float[] mLightModelMatrix = new float[16];

	/** Store our bug model data in a float buffer. */
	private final FloatBuffer mBugPositions;
	private final FloatBuffer mBugColors;
	private final FloatBuffer mBugNormals;
	private final FloatBuffer mBugTextureCoordinates;

	/** Store our bug model data in a float buffer. */
	private final FloatBuffer mFirePositions;
	private final FloatBuffer mFireColors;
	private final FloatBuffer mFireNormals;
	private final FloatBuffer mFireTextureCoordinates;

	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;

	/** This will be used to pass in the modelview matrix. */
	private int mMVMatrixHandle;

	/** This will be used to pass in the light position. */
	private int mLightPosHandle;

	/** This will be used to pass in the texture. */
	private int mTextureUniformHandle;

	/** This will be used to pass in bug model position information. */
	private int mBugPositionHandle;

	/** This will be used to pass in bug model color information. */
	private int mBugColorHandle;

	/** This will be used to pass in bug model normal information. */
	private int mBugNormalHandle;

	/** This will be used to pass in bug model texture coordinate information. */
	private int mBugTextureCoordinateHandle;

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

	/**
	 * Used to hold a light centered on the origin in model space. We need a 4th
	 * coordinate so we can get translations to work when we multiply this by
	 * our transformation matrices.
	 */
	private final float[] mLightPosInModelSpace = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };

	/**
	 * Used to hold the current position of the light in world space (after
	 * transformation via model matrix).
	 */
	private final float[] mLightPosInWorldSpace = new float[4];

	/**
	 * Used to hold the transformed position of the light in eye space (after
	 * transformation via modelview matrix)
	 */
	private final float[] mLightPosInEyeSpace = new float[4];

	/** This is a handle to our bug shading program. */
	private int mBugProgramHandle;

	/** This is a handle to our light point program. */
	private int mLightProgramHandle;

	/** This is a handle to our bug shading program. */
	private int mFireProgramHandle;

	/** This will be used to pass in fire model position information. */
	private int mFirePositionHandle;

	/** This will be used to pass in fire model color information. */
	private int mFireColorHandle;

	/** This will be used to pass in fire model normal information. */
	private int mFireNormalHandle;

	/** This will be used to pass in fire model texture coordinate information. */
	private int mFireTextureCoordinateHandle;

	/** This is a handle to our bug's texture data. */
	private int mBugTextureDataHandle;

	/** This is a handle to our bug's texture data. */
	private int mBugBouncingTextureDataHandle;

	/** This is a handle to our fire's texture data 1. */
	private int mFireTextureDataHandle1;

	/** This is a handle to our fire's texture data 2. */
	private int mFireTextureDataHandle2;

	/** This is a handle to our fire's texture data 3. */
	private int mFireTextureDataHandle3;

	/** This is a handle to our fire's texture data 4. */
	private int mFireTextureDataHandle4;

	/** This is a handle to our fire's texture data 5. */
	private int mFireTextureDataHandle5;

	/** Hold all the fire flames that should be rendered */
	public static List<OpenGLFire> mFireList = new ArrayList<OpenGLFire>();

	/** Random instance */
	public Random rd;

	/** The width of screen, in pixels */
	public static int screenOpenGLWidth;

	/** The height of screen, in pixels */
	public static int screenOpenGLHeight;

	// /** Record the last time we update the speed/position of the bug in
	// opengl */
	// private long lastRefreshBugTime = -1;

	// /** To simulate the reality, bug should halt sometimes */
	// private boolean bugShouldPause = false;
	//
	// /** Hold the lines that bugs should be avoided from */
	// public ArrayList<BorderLine> borderLineList;

	/** Initialize the model data */
	public OpenGLRenderer(final Context activityContext) {
		this.mActivityContext = activityContext;
		this.mCameraActivityInstance = (MainActivity) mActivityContext;
		OpenGLBugManager.setCameraActivityInstance(mCameraActivityInstance);
		// Initialize the buffers.
		int i = 0;
		BufferedReader buff;
		long now = System.currentTimeMillis();
		float[] cubePositionData = new float[3 * 5580];
		try {
			buff = new BufferedReader(new InputStreamReader(mActivityContext.getAssets().open("vertices")));
			i = 0;
			String tmp = buff.readLine();
			while (tmp != null) {
				if (tmp.equals("")) {
					tmp = buff.readLine();
					continue;
				}
				cubePositionData[i++] = Float.parseFloat(tmp);
				tmp = buff.readLine();
			}
			buff.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBugPositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBugPositions.put(cubePositionData).position(0);

		float[] cubeColorData = new float[4 * 5580];
		Arrays.fill(cubeColorData, 1.0f);
		mBugColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBugColors.put(cubeColorData).position(0);

		float[] cubeNormalData = new float[3 * 5580];
		try {
			buff = new BufferedReader(new InputStreamReader(mActivityContext.getAssets().open("normal")));
			i = 0;
			String tmp = buff.readLine();
			while (tmp != null) {
				if (tmp.equals("")) {
					tmp = buff.readLine();
					continue;
				}
				cubeNormalData[i++] = Float.parseFloat(tmp);
				tmp = buff.readLine();
			}
			buff.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBugNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBugNormals.put(cubeNormalData).position(0);

		float[] cubeTextureCoordinateData = new float[2 * 5580];
		try {
			buff = new BufferedReader(new InputStreamReader(mActivityContext.getAssets().open("texture")));
			i = 0;
			String tmp = buff.readLine();
			while (tmp != null) {
				if (tmp.equals("")) {
					tmp = buff.readLine();
					continue;
				}
				cubeTextureCoordinateData[i++] = Float.parseFloat(tmp);
				tmp = buff.readLine();
			}
			buff.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBugTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBugTextureCoordinates.put(cubeTextureCoordinateData).position(0);
		long cost = System.currentTimeMillis() - now;
		Log.d(TAG, "loading time:" + cost);

		rd = new Random();

		// Load fire data
		final float[] firePositionData = {
				// Front face
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f };

		final float[] fireColorData = {
				// Front face (all white)
				1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };

		final float[] fireNormalData = {
				// Front face
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f };

		final float[] fireTextureCoordinateData = {
				// Front face
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f };

		// Initialize the buffers.
		mFirePositions = ByteBuffer.allocateDirect(firePositionData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mFirePositions.put(firePositionData).position(0);

		mFireColors = ByteBuffer.allocateDirect(fireColorData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mFireColors.put(fireColorData).position(0);

		mFireNormals = ByteBuffer.allocateDirect(fireNormalData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mFireNormals.put(fireNormalData).position(0);

		mFireTextureCoordinates = ByteBuffer.allocateDirect(fireTextureCoordinateData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mFireTextureCoordinates.put(fireTextureCoordinateData).position(0);

	}

	protected String getVertexShader(int resId) {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, resId);
	}

	protected String getFragmentShader(int resId) {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, resId);
	}

	// Position the eye in front of the origin.
	float eyeX = 0.0f;
	float eyeY = 0.0f;
	float eyeZ = 0.0f;

	// We are looking toward the distance
	float lookX = 0.0f;
	float lookY = 0.0f;
	float lookZ = -100.0f;

	// Set our up vector. This is where our head would be pointing were we
	// holding the camera.
	float upX = 0.0f;
	float upY = 1.0f;
	float upZ = 0.0f;

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		Log.d(TAG, "surface screated");
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// The below glEnable() call is a holdover from OpenGL ES 1, and is not
		// needed in OpenGL ES 2.
		// Enable texture mapping
		// GLES20.glEnable(GLES20.GL_TEXTURE_2D);

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.

		final String bugVertexShader = getVertexShader(R.raw.per_pixel_vertex_shader);
		final String bugFragmentShader = getFragmentShader(R.raw.per_pixel_fragment_shader);

		final int bugVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, bugVertexShader);
		final int bugFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, bugFragmentShader);

		mBugProgramHandle = ShaderHelper.createAndLinkProgram(bugVertexShaderHandle, bugFragmentShaderHandle, new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate" });

		// Load the bug's texture
		mBugTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.ladybug, GLES20.GL_TEXTURE0);
		mBugTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.bouncing, GLES20.GL_TEXTURE6);

		// Define a simple shader program for our point.
		final String pointVertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_vertex_shader);
		final String pointFragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_fragment_shader);

		final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
		final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
		mLightProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle, new String[] { "a_Position" });

		// Define fire program
		final String fireVertexShader = getVertexShader(R.raw.per_pixel_vertex_shader);
		final String fireFragmentShader = getFragmentShader(R.raw.per_pixel_fragment_shader_simple);

		final int fireVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, fireVertexShader);
		final int fireFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fireFragmentShader);

		mFireProgramHandle = ShaderHelper.createAndLinkProgram(fireVertexShaderHandle, fireFragmentShaderHandle, new String[] { "a_Position", "a_Color", "a_Normal", "a_TexCoordinate" });

		// Load the fire's texture
		int[] fireResourcesIds = new int[] { R.drawable.fire_1, R.drawable.fire_2, R.drawable.fire_3, R.drawable.fire_4, R.drawable.fire_5 };
		int[] activeTextureNums = new int[] { GLES20.GL_TEXTURE1, GLES20.GL_TEXTURE2, GLES20.GL_TEXTURE3, GLES20.GL_TEXTURE4, GLES20.GL_TEXTURE5 };
		int[] fireTextureHandlesArr = new int[5];
		fireTextureHandlesArr = TextureHelper.loadTextures(mActivityContext, fireResourcesIds, activeTextureNums);

		mFireTextureDataHandle1 = fireTextureHandlesArr[0];
		mFireTextureDataHandle2 = fireTextureHandlesArr[1];
		mFireTextureDataHandle3 = fireTextureHandlesArr[2];
		mFireTextureDataHandle4 = fireTextureHandlesArr[3];
		mFireTextureDataHandle5 = fireTextureHandlesArr[4];
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);
		screenOpenGLWidth = width;
		screenOpenGLHeight = height;

		// Set OpenGLBug's scale
		OpenGLBug.radius = (int) (screenOpenGLWidth / (SCALE_RATIO * 2));

		eyeX = screenOpenGLWidth / 2;
		eyeY = screenOpenGLHeight / 2;
		lookX = screenOpenGLWidth / 2;
		lookY = screenOpenGLHeight / 2;

		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		final float left = -width / 2;
		final float right = width / 2;
		final float bottom = -height / 2;
		final float top = height / 2;
		final float near = 0.001f;
		final float far = 3000.0f;

		Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	}

	public volatile float globalRotateDegree = 0;
	// public float distanceX;
	// public float distanceY;

	/** This variable will be incremented every frame. */
	public int fireDrawCount = 1;

	/** This indicates which mode the game is now at */
	public int openGlMode = 0;

	@Override
	public void onDrawFrame(GL10 glUnused) {
		// Clear background to transparency
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Calculate current milliseconds
		long now = SystemClock.uptimeMillis() % 10000L;
		// Calculate rotate degree for some use
		// float angleInDegrees = (360.0f / 10000.0f) * ((int) now);

		// Set our per-vertex bug program.
		GLES20.glUseProgram(mBugProgramHandle);

		// Load the res handles that will be used in drawing bugs.
		loadOpenGLBugResHandles(mBugProgramHandle);

		// Draw bugs
		drawBugs();

		// Set our per-vertex fire program.
		GLES20.glUseProgram(mFireProgramHandle);

		// Load the res handles that will be used in drawing fire.
		loadOpenGLFireResHandles(mFireProgramHandle);

		// Calculate which fire texture should be binded.
		int fireTextureNum = fireDrawCount / 10 + 1;
		fireDrawCount++;
		if (fireDrawCount > 49)
			fireDrawCount = 1;

		// Tell the texture uniform sampler to use this texture in the
		// shader by binding to texture unit fireTextureNum.
		GLES20.glUniform1i(mTextureUniformHandle, fireTextureNum);

		// Draw fires
		drawFires();

	}

	/**
	 * Load the res handles that will be used in drawing bugs.
	 * 
	 * @param handle
	 *            The handle that you want to bind
	 */
	private void loadOpenGLBugResHandles(int handle) {
		mMVMatrixHandle = GLES20.glGetUniformLocation(handle, "u_MVMatrix");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(handle, "u_MVPMatrix");
		mTextureUniformHandle = GLES20.glGetUniformLocation(handle, "u_Texture");
		mLightPosHandle = GLES20.glGetUniformLocation(handle, "u_LightPos");

		// Set program handles for bug drawing.
		mBugPositionHandle = GLES20.glGetAttribLocation(handle, "a_Position");
		mBugColorHandle = GLES20.glGetAttribLocation(handle, "a_Color");
		mBugNormalHandle = GLES20.glGetAttribLocation(handle, "a_Normal");
		mBugTextureCoordinateHandle = GLES20.glGetAttribLocation(handle, "a_TexCoordinate");
	}

	/** Draws bugs in mBugList */
	private void drawBugs() {
		ListIterator<OpenGLBug> mOpenGLBugListIterator = OpenGLBugManager.getListIterator();

		while (mOpenGLBugListIterator.hasNext()) {
			OpenGLBug mOpenGLBug = mOpenGLBugListIterator.next();

			// Tell the texture uniform sampler to use this texture in the
			// shader by binding to texture unit 0.
			GLES20.glUniform1i(mTextureUniformHandle, 0);
			// // If the bug is boucing, we should let it be blue.
			// if (!mOpenGLBug.bouncing) {
			// } else {
			// GLES20.glUniform1i(mTextureUniformHandle, 6);
			// }

			// Load the model matrix as identity matrix
			Matrix.setIdentityM(mModelMatrix, 0);

			// Our projection is ortho.
			Matrix.translateM(mModelMatrix, 0, mOpenGLBug.x, mOpenGLBug.y, -500.0f);

			// Rotate to bug's speed direction
			Matrix.rotateM(mModelMatrix, 0, headRotate(mOpenGLBug.speedX, mOpenGLBug.speedY), 0, 0, -1.0f);
			// Rotate in order to make the bug's back facing us.
			Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
			Matrix.rotateM(mModelMatrix, 0, 90, -1.0f, 0.0f, 0.0f);

			// The original 3d obj model is too small. So we scale it by
			// screenWidth/SCALE_RATIO times. That's also why we set
			// OpenGLBug.radius to screenOpenGLWidth/(SCALE_RATIO * 2)
			Matrix.scaleM(mModelMatrix, 0, screenOpenGLWidth / mOpenGLBug.scaleRatio, screenOpenGLWidth / mOpenGLBug.scaleRatio, 100f);

			// Pass in the position information
			mBugPositions.position(0);
			GLES20.glVertexAttribPointer(mBugPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mBugPositions);

			GLES20.glEnableVertexAttribArray(mBugPositionHandle);

			// Pass in the color information
			mBugColors.position(0);
			GLES20.glVertexAttribPointer(mBugColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mBugColors);

			GLES20.glEnableVertexAttribArray(mBugColorHandle);

			// Pass in the normal information
			mBugNormals.position(0);
			GLES20.glVertexAttribPointer(mBugNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mBugNormals);

			GLES20.glEnableVertexAttribArray(mBugNormalHandle);

			// If bug is burning, make it black!
			if (!mOpenGLBug.burning) {
				// Pass in the texture coordinate information
				mBugTextureCoordinates.position(0);
				GLES20.glVertexAttribPointer(mBugTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mBugTextureCoordinates);
				GLES20.glEnableVertexAttribArray(mBugTextureCoordinateHandle);
			}

			// This multiplies the view matrix by the model matrix, and stores
			// the
			// result in the MVP matrix
			// (which currently contains model * view).
			Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

			// Pass in the modelview matrix.
			GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

			// This multiplies the modelview matrix by the projection matrix,
			// and
			// stores the result in the MVP matrix
			// (which now contains model * view * projection).
			Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

			// Pass in the combined matrix.
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

			// Pass in the light position in eye space.
			GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

			// Draw the cube.
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 5580);

			// Rerfresh the bug status
			mOpenGLBug.refresh(mOpenGLBugListIterator);
		}

	}

	/**
	 * Load the res handles that will be used in drawing fires.
	 * 
	 * @param handle
	 *            The handle that you want to bind
	 */
	private void loadOpenGLFireResHandles(int handle) {
		mMVMatrixHandle = GLES20.glGetUniformLocation(handle, "u_MVMatrix");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(handle, "u_MVPMatrix");
		mTextureUniformHandle = GLES20.glGetUniformLocation(handle, "u_Texture");
		mLightPosHandle = GLES20.glGetUniformLocation(handle, "u_LightPos");

		// Set program handles for bug drawing.
		mFirePositionHandle = GLES20.glGetAttribLocation(handle, "a_Position");
		mFireColorHandle = GLES20.glGetAttribLocation(handle, "a_Color");
		mFireNormalHandle = GLES20.glGetAttribLocation(handle, "a_Normal");
		mFireTextureCoordinateHandle = GLES20.glGetAttribLocation(handle, "a_TexCoordinate");
	}

	/** Draw fires in mFireList */
	private void drawFires() {
		for (OpenGLFire mOpenGLFire : mFireList) {
			// Load the model matrix as identity matrix
			Matrix.setIdentityM(mModelMatrix, 0);

			// Our projection is ortho.
			Matrix.translateM(mModelMatrix, 0, (float) mOpenGLFire.ratioX * screenOpenGLWidth, (float) mOpenGLFire.ratioY * screenOpenGLHeight, -500.0f);

			// The original coordinate model is too small. So we scale it by
			// screenWidth/SCALE_RATIO
			// times.
			Matrix.scaleM(mModelMatrix, 0, screenOpenGLWidth / SCALE_RATIO, screenOpenGLWidth / SCALE_RATIO, 100f);

			// Pass in the position information
			mFirePositions.position(0);
			GLES20.glVertexAttribPointer(mFirePositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mFirePositions);

			GLES20.glEnableVertexAttribArray(mFirePositionHandle);

			// Pass in the color information
			mFireColors.position(0);
			GLES20.glVertexAttribPointer(mFireColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mFireColors);

			GLES20.glEnableVertexAttribArray(mFireColorHandle);

			// Pass in the normal information
			mFireNormals.position(0);
			GLES20.glVertexAttribPointer(mFireNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mFireNormals);

			GLES20.glEnableVertexAttribArray(mFireNormalHandle);

			// Pass in the texture coordinate information
			mFireTextureCoordinates.position(0);
			GLES20.glVertexAttribPointer(mFireTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mFireTextureCoordinates);

			GLES20.glEnableVertexAttribArray(mFireTextureCoordinateHandle);

			// This multiplies the view matrix by the model matrix, and stores
			// the
			// result in the MVP matrix
			// (which currently contains model * view).
			Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

			// Pass in the modelview matrix.
			GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

			// This multiplies the modelview matrix by the projection matrix,
			// and
			// stores the result in the MVP matrix
			// (which now contains model * view * projection).
			Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

			// Pass in the combined matrix.
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

			// Pass in the light position in eye space.
			GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

			// Draw the cube.
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		}
	}

	/* Bug rendering */
	/** Determine the bug's head rotation */
	private float headRotate(int speedX, int speedY) {
		float rotateDegree = 0;
		double rotateRadian;
		if (speedX == 0) {
			if (speedY >= 0)
				rotateRadian = 0;
			else
				rotateRadian = Math.PI;
		} else {
			rotateRadian = Math.atan(Math.abs((double) speedY / speedX));
		}

		rotateDegree = (float) (Math.toDegrees(rotateRadian));

		if (speedX > 0 && speedY <= 0) {
			rotateDegree = 90 + rotateDegree;
		}
		if (speedX > 0 && speedY > 0) {
			rotateDegree = 90 - rotateDegree;
		}
		if (speedX < 0 && speedY <= 0) {
			rotateDegree = 90 + rotateDegree;
			rotateDegree = -rotateDegree;
		}
		if (speedX < 0 && speedY > 0) {
			rotateDegree = 90 - rotateDegree;
			rotateDegree = -rotateDegree;
		}
		return rotateDegree;
	}

}
