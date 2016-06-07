package draw.heshi.com.drawpane.views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import draw.heshi.com.drawpane.R;
import draw.heshi.com.drawpane.views.event.OnBrushPickerSelectedListener;


public class BrushPicker extends DialogFragment {
    private float selectedBrushSize;
    private OnBrushPickerSelectedListener mListener;
    private SeekBar brushSizeSeekBar;
    private TextView minValue, mxValue, currentValue;
    private int currentBrushSize ;

    /**
     *
     * @param listener an implementation of the listener
     *
     */
    public void setOnBrushPickerSelectedListener(
            OnBrushPickerSelectedListener listener){
        mListener = listener;
    }

    public static BrushPicker NewInstance(int size){
        BrushPicker fragment = new BrushPicker();
        Bundle args = new Bundle();
        if (size > 0){
            args.putInt("current_brush_size", size);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey("current_brush_size")){
            int brushSize = args.getInt("current_brush_size", 0);
            if (brushSize > 0){
                currentBrushSize = brushSize;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Begin building a new dialog.
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater.
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate the layout for this dialog.
        final View dialogView = inflater.inflate(R.layout.dialog_brush_size_chooser, null);
        if (dialogView != null) {
            //set the starting value of the seek bar for visual aide
            minValue = (TextView)dialogView.findViewById(R.id.text_view_min_value);
            int minSize = getResources().getInteger(R.integer.min_size);
            minValue.setText(minSize + "");

            mxValue = (TextView)dialogView.findViewById(R.id.text_view_mx_value);
            mxValue.setText(String.valueOf(getResources().getInteger(R.integer.max_size)));


            currentValue = (TextView)dialogView.findViewById(R.id.text_view_brush_size);
            if (currentBrushSize > 0){
                currentValue.setText(getResources().getString(R.string.label_brush_size) + currentBrushSize);
            }

            brushSizeSeekBar = (SeekBar)dialogView.findViewById(R.id.seek_bar_brush_size);
            brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChanged = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChanged = progress;
                    currentValue.setText(getResources().getString(R.string.label_brush_size) + progress);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mListener.OnNewBrushSizeSelected(progressChanged);
                }
            });

        }

        builder.setTitle("Choose new Brush Size")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(dialogView);


        return builder.create();

    }

}
