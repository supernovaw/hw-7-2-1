package com.example.homework721;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
	private TextView infoLabel;
	private EditText locationInput;
	private Button openMapsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();

		WorldRegionsMap.initIfNecessary(getResources());

		locationInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				MainActivity.this.onTextChanged(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		locationInput.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				proceedToMaps();
				return true;
			}
			return false;
		});
		openMapsButton.setOnClickListener(v -> proceedToMaps());
	}

	private void initViews() {
		locationInput = findViewById(R.id.locationInput);
		infoLabel = findViewById(R.id.infoLabel);
		openMapsButton = findViewById(R.id.openMapsButton);
	}

	private void onTextChanged(String text) {
		double[] coords = WorldRegionsMap.getCoordinatesFromString(text);
		if (coords == null) {
			infoLabel.setText(R.string.coords_cannot_be_interpreted);
		} else {
			String coordsString = WorldRegionsMap.coordsToString(coords);
			String region = WorldRegionsMap.getRegionFromCoords(coords, getResources());
			infoLabel.setText(getString(R.string.region_correspondence, coordsString, region));
		}
	}

	private void proceedToMaps() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(getGeoUri());
		startActivity(intent);
	}

	private Uri getGeoUri() {
		String in = locationInput.getText().toString();
		double[] coords = WorldRegionsMap.getCoordinatesFromString(in);
		if (coords == null)
			return Uri.parse("geo:?q=" + in);
		else
			return Uri.parse("geo:" + coords[0] + "," + coords[1]);
	}
}
