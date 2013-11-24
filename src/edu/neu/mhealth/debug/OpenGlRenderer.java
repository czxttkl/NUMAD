package edu.neu.mhealth.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
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
	private int mProgramHandle;

	/** This is a handle to our light point program. */
	private int mPointProgramHandle;

	/** This is a handle to our texture data. */
	private int mTextureDataHandle;
	private int mTextureDataHandle1;
	private int mTextureDataHandle2;

	/** This indicates which mode the game is now at */
	public int mode = 0;
	public int MODE_MAIN_MENU = 0;
	public int MODE_TUTORIAL = 1;

	/**
	 * Initialize the model data.
	 */
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
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		final String vertexShader = getVertexShader();
		final String fragmentShader = getFragmentShader();

		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[] {
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

		// Load the texture
		mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.ladybug);
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		// mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
		// R.drawable.bumpy_bricks_public_domain);
		// mTextureDataHandle2 = TextureHelper.loadTexture(mActivityContext,
		// R.drawable.bumpy_bricks_public_domain);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

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

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		// Set our per-vertex bug program.
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
		long time = SystemClock.uptimeMillis() % 10000L;
		switch (mode) {
		// Main Menu mode
		case 0:
			Log.d(TAG, "mode 0:mainmenu");
			float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
			// Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
			GLES20.glUniform1i(mTextureUniformHandle, 0);
			
			Matrix.setIdentityM(mModelMatrix, 0);
			Matrix.translateM(mModelMatrix, 0, 2.0f, 0.0f, -500.0f);
			Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
			Matrix.rotateM(mModelMatrix, 0, 90, -1.0f, 0.0f, 0.0f);
			Matrix.scaleM(mModelMatrix, 0, 100f, 100f, 100f);
			// Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 0.0f, -1.0f);
			drawBug();
			break;
		// Tutorial mode
		case 1:
			Log.d(TAG, "mode 1:tutorial");
			Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, eyeX, eyeY, lookZ, upX, upY, upZ);
			break;
		case 2:
			break;
		default:
			Log.d(TAG, "wrong mode");
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

		// Calculate position of the light. Rotate and then push into th distance.
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
}
