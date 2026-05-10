package com.koteinik.chunksfadein.compat.dh;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class DHBlazeState {
	public static final int LOD_MASK_LOC = 14;
	public static final int SKY_LOC = 15;

	public static final int[] terrainPrograms = new int[] { -1, -1 };

	public static int screenSize = -1;
	public static int dhFadeActive = -1;
	public static int dhStartFadeBlockDistanceSq = -1;
	public static int lodMaskDim = -1;
	public static int lodMaskMaxDist = -1;
	public static int lodMaskOrigin = -1;
	public static int lodMaskMinY = -1;
	public static int chunkFadeData = -1;

	public static void registerProgram(int programId) {
		for (int id : terrainPrograms)
			if (id == programId) return;

		for (int i = 0; i < terrainPrograms.length; i++) {
			if (terrainPrograms[i] != -1) continue;
			terrainPrograms[i] = programId;

			screenSize = glGetUniformLocation(programId, "cfi_screenSize");
			dhFadeActive = glGetUniformLocation(programId, "cfi_dhFadeActive");
			dhStartFadeBlockDistanceSq = glGetUniformLocation(programId, "cfi_dhStartFadeBlockDistanceSq");
			lodMaskDim = glGetUniformLocation(programId, "cfi_lodMaskDim");
			lodMaskMaxDist = glGetUniformLocation(programId, "cfi_lodMaskMaxDist");
			lodMaskOrigin = glGetUniformLocation(programId, "cfi_lodMaskOrigin");
			lodMaskMinY = glGetUniformLocation(programId, "cfi_lodMaskMinY");
			chunkFadeData = glGetUniformLocation(programId, "cfi_chunkFadeData");

			return;
		}
	}

	public static boolean hasAnyProgram() {
		for (int id : terrainPrograms)
			if (id != -1) return true;

		return false;
	}
}
