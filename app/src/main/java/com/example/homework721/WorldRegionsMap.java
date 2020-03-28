package com.example.homework721;

import android.content.res.Resources;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* Converts coordinates string to longitude
 * and latitude and decides which region the
 * given coordinates correspond to (i.e. Asia)
 */
public final class WorldRegionsMap {
	/* Map values:
	 * 1	Europe
	 * 2	Africa
	 * 3	Asia
	 * 4	Australia
	 * 5	New Zealand
	 * 6	North America
	 * 7	South America
	 * 8	Antarctica
	 * 9	Arctic Ocean
	 * 10	Pacific Ocean
	 * 11	Atlantic Ocean
	 * 12	Indian Ocean
	 *
	 * no other values are stored in the map
	 */
	private static int[][] map;

	// returns null for incorrect input or 2 number array with latitude and longitude
	static double[] getCoordinatesFromString(String input) {
		input = input.replace(';', ',');
		input = input.toUpperCase();

		int split = input.indexOf(',');
		if (split == -1)
			return null;

		String latitudeString = input.substring(0, split).trim();
		String longitudeString = input.substring(split + 1).trim();

		if (latitudeString.isEmpty() || longitudeString.isEmpty())
			return null;

		double latitude = getCoordinateValue(latitudeString, 'S', 'N');
		double longitude = getCoordinateValue(longitudeString, 'W', 'E');

		if (Double.isNaN(latitude) || Double.isNaN(longitude))
			return null;
		if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)
			return null;

		Log.v("WorldRegionsMap", latitude + ";" + longitude);
		return new double[]{latitude, longitude};
	}

	// input array: [latitude, longitude]
	static String getRegionFromCoords(double[] coords, Resources res) {
		if (coords.length != 2)
			throw new IllegalArgumentException("For coords size " + coords.length);

		double latitude = coords[0];
		double longitude = coords[1];

		// mod and min operations are for the cases like -90;180
		int arrayX = (int) ((longitude + 180)) % 360;
		int arrayY = (int) Math.min(90 - latitude, 179);

		int value = map[arrayX][arrayY];
		// as value is in range [1;12] and indices are [0;11] it has to be decremented by 1
		return res.getStringArray(R.array.region_names)[value - 1];
	}

	// input array: [latitude, longitude]
	static String coordsToString(double[] coords) {
		if (coords.length != 2)
			throw new IllegalArgumentException("For coords size " + coords.length);
		double lat = coords[0];
		double lon = coords[1];
		String latitude = Math.abs(lat) + (lat < 0 ? " S" : " N");
		String longitude = Math.abs(lon) + (lon < 0 ? " W" : " E");
		return latitude + ", " + longitude;
	}

	/* example inputs:
	 * st="139.45E", negative='W', positive='E'
	 * st="N45", negative='S', positive='N'
	 * st="-50.35", negative='S', positive='N'
	 *
	 * returns NaN if input is incorrect
	 */
	private static double getCoordinateValue(String st, char negative, char positive) {
		char firstChar = st.charAt(0), lastChar = st.charAt(st.length() - 1);

		boolean signLetterStart = firstChar == negative || firstChar == positive;
		boolean signLetterEnd = lastChar == negative || lastChar == positive;
		boolean signNormal = firstChar == '+' || firstChar == '-';

		if (signLetterStart && signLetterEnd) // i.e. N45.04S
			return Double.NaN;
		if ((signLetterStart || signLetterEnd) && signNormal) // i.e. -150W
			return Double.NaN;
		/* After 2 if's, st has either normal sign (+45.3),
		 * 1 letter sign (45.3N or S45.3) or no sign (45.3)
		 */

		if (!signLetterStart && !signLetterEnd && !signNormal) { // no sign
			try {
				return Double.parseDouble(st);
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}

		if (signNormal) {
			double value;
			try {
				value = Double.parseDouble(st.substring(1)); // cut first char
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
			if (firstChar == '-')
				return -value; // i.e. -45.3
			else
				return value; // i.e. +45.3
		}

		// after 2 if's there is only case left with letter sign (i.e. 45.3N)
		if (signLetterStart) { // sign as first char
			double value;
			try {
				value = Double.parseDouble(st.substring(1)); // cut first char
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
			if (firstChar == positive)
				return value;
			else
				return -value;
		} else { // sign as last char
			double value;
			try {
				value = Double.parseDouble(st.substring(0, st.length() - 1)); // cut last char
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
			if (lastChar == positive)
				return value;
			else
				return -value;
		}
	}

	static void initIfNecessary(Resources res) {
		if (map != null)
			return;

		map = new int[360][180];
		try (InputStream in = res.openRawResource(R.raw.world_regions);
			 ByteArrayOutputStream outBuffer = new ByteArrayOutputStream()) {
			int read;
			byte[] buffer = new byte[0x10000]; // 64 KB
			while ((read = in.read(buffer)) != -1)
				outBuffer.write(buffer, 0, read);

			byte[] readData = outBuffer.toByteArray();
			for (int y = 0, i = 0; y < 180; y++) {
				for (int x = 0; x < 360; x++) {
					map[x][y] = readData[i++];
				}
			}

		} catch (IOException e) {
			Log.w("MainActivity",
					"IOException while reading world_regions.bin: " + e.getMessage());
		}
	}
}
