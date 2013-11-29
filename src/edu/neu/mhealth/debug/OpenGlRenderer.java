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
import edu.neu.mhealth.debug.opengl.OpenGLFire;

/**
 * This class implements our custom renderer. Note that the GL10 parameter
 * passed in is unused for OpenGL ES 2.0 renderers -- the static class GLES20 is
 * used instead.
 */
public class OpenGlRenderer implements GLSurfaceView.Renderer {
	/** Used for debug logs. */
	private static final String TAG = "mDebug";

	private final Context mActivityContext;

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
	private int mBugHandle;

	/** This is a handle to our light point program. */
	private int mPointProgramHandle;

	/** This is a handle to our bug's texture data. */
	private int mBugTextureDataHandle;
	
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
	public ArrayList<OpenGLFire> mFireList = new ArrayList<OpenGLFire>();
	
	/** Random instance */
	public Random rd;
	
	/** The width of screen, in pixels */
	private int screenWidth;
	
	/** The height of screen, in pixels */
	private int screenHeight;
	
	/** Hold all the bugs that should be counted and calculated */
	private ArrayList<OpenGLBug> mBugList = new ArrayList<OpenGLBug>();
	
	/** Record the last time we update the speed/position of the bug in opengl */
	private long lastRefreshBugTime = -1;
	
	/** To simulate the reality, bug should halt sometimes */
	private boolean bugShouldPause = false;
	
	/** Hold the lines that bugs should be avoided from */
	public ArrayList<BorderLine> borderLineList;

	/** Initialize the model data*/
	public OpenGlRenderer(final Context activityContext) {
		mActivityContext = activityContext;
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
		mBugPositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBugPositions.put(cubePositionData).position(0);

		float[] cubeColorData = new float[4 * 5580];
		Arrays.fill(cubeColorData, 1.0f);
		mBugColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
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
		mBugNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
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
		mBugTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mBugTextureCoordinates.put(cubeTextureCoordinateData).position(0);
		long cost = System.currentTimeMillis() - now;
		Log.d(TAG, "loading time:" + cost);

		rd = new Random();
	}

	protected String getVertexShader() {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader);
	}

	protected String getFragmentShader() {
		return RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader);
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

		final String bugVertexShader = getVertexShader();
		final String bugFragmentShader = getFragmentShader();

		final int bugVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, bugVertexShader);
		final int bugFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, bugFragmentShader);

		mBugHandle = ShaderHelper.createAndLinkProgram(bugVertexShaderHandle, bugFragmentShaderHandle, new String[] {
				"a_Position", "a_Color", "a_Normal", "a_TexCoordinate" });

		// Define a simple shader program for our point.
		final String pointVertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext,
				R.raw.point_vertex_shader);
		final String pointFragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext,
				R.raw.point_fragment_shader);

		final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
		final int pointFragmentShaderHandle = ShaderHelper
				.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
		mPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
				new String[] { "a_Position" });

		
		// Load the bug's texture
		mBugTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.ladybug, GLES20.GL_TEXTURE0);
		
		int[] fireResourcesIds = new int[] {R.drawable.fire_1, R.drawable.fire_2, R.drawable.fire_3, R.drawable.fire_4, R.drawable.fire_5};
		int[] activeTextureNums = new int[] {GLES20.GL_TEXTURE1, GLES20.GL_TEXTURE2, GLES20.GL_TEXTURE3, GLES20.GL_TEXTURE4, GLES20.GL_TEXTURE5};
		int[] fireTextureHandlesArr = new int[5];
		fireTextureHandlesArr = TextureHelper.loadTextures(mActivityContext, fireResourcesIds, activeTextureNums);
		
		mFireTextureDataHandle1 = fireTextureHandlesArr[0];
		mFireTextureDataHandle2 = fireTextureHandlesArr[1];
		mFireTextureDataHandle3 = fireTextureHandlesArr[2];
		mFireTextureDataHandle4 = fireTextureHandlesArr[3];
		mFireTextureDataHandle5 = fireTextureHandlesArr[4];
		// Bind the texture to this unit.
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBugTextureDataHandle);
//		
//		Log.d(TAG, "czx texture #:" + GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
//		mFireTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext, R.drawable.fire_1);
//		mFireTextureDataHandle2 = TextureHelper.loadTexture(mActivityContext, R.drawable.fire_2);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);
		screenWidth = width;
		screenHeight = height;
		Log.d(TAG, "screenWidth&Height:" + screenWidth + "," + screenHeight);
		eyeX = screenWidth / 2;
		eyeY = screenHeight / 2;
		lookX = screenWidth / 2;
		lookY = screenHeight / 2;
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		// final float ratio = (float) width / height;
		final float left = -width / 2;
		final float right = width / 2;
		final float bottom = -height / 2;
		final float top = height / 2;
		final float near = 0.001f;
		final float far = 3000.0f;

		Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	}

	public volatile float globalRotateDegree = 0;
	public float distanceX;
	public float distanceY;
	public int fire = 1;
	/** This indicates which mode the game is now at */
	public int openGlMode = 0;
	public final int MODE_MAIN_MENU = 1;
	public final int MODE_TUTORIAL_1 = 2;
	public final int MODE_DEFAULT = 0;

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		// Set our per-vertex bug program.
		GLES20.glUseProgram(mBugHandle);
		// Set program handles for cube drawing.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mBugHandle, "u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mBugHandle, "u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mBugHandle, "u_LightPos");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mBugHandle, "u_Texture");
		mPositionHandle = GLES20.glGetAttribLocation(mBugHandle, "a_Position");
		mColorHandle = GLES20.glGetAttribLocation(mBugHandle, "a_Color");
		mNormalHandle = GLES20.glGetAttribLocation(mBugHandle, "a_Normal");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mBugHandle, "a_TexCoordinate");
		long now = SystemClock.uptimeMillis() % 10000L;
		
		switch (openGlMode) {
		
		// Main Menu mode
		case MODE_MAIN_MENU:
			Log.d(TAG, "mode 0:mainmenu");
			float angleInDegrees = (360.0f / 10000.0f) * ((int) now);
			// Tell the texture uniform sampler to use this texture in the
			// shader by binding to texture unit 0.
			GLES20.glUniform1i(mTextureUniformHandle, 0);
			// We only want one bug in the main menu
			if (mBugList.size() != 1) {
				mBugList.clear();
			}
			if (mBugList.size() == 0) {
				int randomHeight = rd.nextInt(screenHeight / 3);
				OpenGLBug menuBug = new OpenGLBug(screenWidth - OpenGLBug.radius, screenHeight / 4 + randomHeight, -1,
						1);
				mBugList.add(menuBug);
			}

			OpenGLBug menuBug = mBugList.get(0);
			Matrix.setIdentityM(mModelMatrix, 0);
			Matrix.translateM(mModelMatrix, 0, menuBug.x, menuBug.y, -500.0f);
			Matrix.rotateM(mModelMatrix, 0, headRotate(menuBug.speedX, menuBug.speedY), 0, 0, -1.0f);
			Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
			Matrix.rotateM(mModelMatrix, 0, 90, -1.0f, 0.0f, 0.0f);
			Matrix.scaleM(mModelMatrix, 0, 100f, 100f, 100f);
			// Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 0.0f, -1.0f);
			drawBug();
			menuBug = refreshBug(menuBug);

			break;
			
		// Tutorial 1 mode
		case MODE_TUTORIAL_1:
			Log.d(TAG, "mode 1:tutorial");
			Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, eyeX, eyeY, lookZ, upX, upY, upZ);
			break;
			
		default:
			// Don't render anything
		}

		// upX = (float)
		// (Math.abs(Math.tan(Math.toRadians(globalRotateDegree)))) ;
		//
		// if (globalRotateDegree >= 180 && globalRotateDegree <360) {
		// upX = - upX;
		// }
		//
		// if (globalRotateDegree >=90 && globalRotateDegree <270) {
		// upY = -1.0f;
		// } else {
		// upY = 1.0f;
		// }

		// switch(fire) {
		// case 1:
		// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		// break;
		// case 2:
		// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);
		// break;
		// case 3:
		// GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle2);
		// break;
		// }
		// fire = (fire + 1) % 3;

		// Calculate position of the light. Rotate and then push into th
		// distance.
		Matrix.setIdentityM(mLightModelMatrix, 0);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
		Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
		// Draw a point to indicate the light.
		GLES20.glUseProgram(mPointProgramHandle);
		drawLight();
	}

	/**
	 * Draws a cube.
	 */
	private void drawBug() {
		// Pass in the position information
		mBugPositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mBugPositions);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information
		mBugColors.position(0);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mBugColors);

		GLES20.glEnableVertexAttribArray(mColorHandle);

		// Pass in the normal information
		mBugNormals.position(0);
		GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mBugNormals);

		GLES20.glEnableVertexAttribArray(mNormalHandle);

		// Pass in the texture coordinate information
		mBugTextureCoordinates.position(0);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
				mBugTextureCoordinates);

		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// Pass in the modelview matrix.
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Pass in the light position in eye space.
		GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

		// Draw the cube.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 5580);
	}

	/**
	 * Draws a point representing the position of the light.
	 */
	private void drawLight() {
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
		final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1],
				mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for
		// this attribute.
		GLES20.glDisableVertexAttribArray(pointPositionHandle);

		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	private float headRotate(int speedX, int speedY) {
		float rotateDegree = 0;
		rotateDegree = (float) Math.toDegrees(Math.atan(Math.abs(speedY / speedX)));
		if (speedX > 0 && speedY < 0) {
			rotateDegree = 90 + rotateDegree;
		}
		if (speedX > 0 && speedY > 0) {
			rotateDegree = 90 - rotateDegree;
		}
		if (speedX < 0 && speedY < 0) {
			rotateDegree = 90 + rotateDegree;
			rotateDegree = -rotateDegree;
		}
		if (speedX < 0 && speedY > 0) {
			rotateDegree = 90 - rotateDegree;
			rotateDegree = -rotateDegree;
		}
		return rotateDegree;
	}

	private OpenGLBug refreshBug(OpenGLBug bug) {
		// That means CameraActivity hasn't initialized the border lines.
		// if (borderLineList == null)
		// return bug;

		int polarityX = bug.speedX >= 0 ? 1 : -1;
		int polarityY = bug.speedY >= 0 ? 1 : -1;
		int tmpX = bug.x + bug.speedX;
		int tmpY = bug.y + bug.speedY;

		if (bugShouldPause) {
			if (System.currentTimeMillis() - lastRefreshBugTime > 2000) {
				bugShouldPause = false;
			}
			return bug;
		}
		if (rd.nextInt(100) > 98) {
			bugShouldPause = true;
			return bug;
		}

		// Log.d(TAG, "random:" + randomSubSpeedX + "," + randomSubSpeedY);
		// for (BorderLine bl : borderLineList) {
		if (tmpX + polarityX * bug.radius > screenWidth || tmpX + polarityX * bug.radius < 0) {
			bug.speedX = -bug.speedX;
			tmpX = bug.x;
		}
		if (tmpY + polarityY * bug.radius > screenHeight || tmpY + polarityY * bug.radius < 0) {
			bug.speedY = -bug.speedY;
			tmpY = bug.y;
		}
		// }

		bug.x = tmpX;
		bug.y = tmpY;
		lastRefreshBugTime = System.currentTimeMillis();
		return bug;
	}
}
