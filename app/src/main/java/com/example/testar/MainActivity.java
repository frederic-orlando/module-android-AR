package com.example.testar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Double MIN_OPENGL_VERSION = 3.0;

    ArFragment arFragment;
    ModelRenderable modelRenderable;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_main);

        initAR();
    }

    private void initAR() {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        ModelRenderable.builder()
                .setSource(this, Uri.parse("Desk.sfb"))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast toast = Toast.makeText(this, "Unable to load models", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

        arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
            if (modelRenderable == null) {
                return;
            }

            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);

            anchorNode.setParent(arFragment.getArSceneView().getScene());

            TransformableNode desk = new TransformableNode(arFragment.getTransformationSystem());
            desk.setParent(anchorNode);
            desk.setRenderable(modelRenderable);
            desk.select();
        });
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        // Check Android Version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(activity, "Requires Android N or Later", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }

        // Check OPENGL Version
        String opengGLVersionString = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                .getDeviceConfigurationInfo()
                .getGlEsVersion();
        if (Double.parseDouble(opengGLVersionString) < MIN_OPENGL_VERSION) {
            Toast.makeText(activity, "Requires OPENGL 3.0 or Later", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
        return true;
    }

}