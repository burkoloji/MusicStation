package com.example.musicstation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import dyanamitechetan.vusikview.VusikView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {

    private ImageButton btn_play_pause;
    private SeekBar seekBar;
    private FusedLocationProviderClient client;

    Handler handler4location=new Handler();
    Timer timer;


    private VusikView musicView;


    private MediaPlayer mediaPlayer;
    private int mediaFileLength;
    private int realtimeLenght;

    final Handler handler = new Handler();
    Double d_enlem;
    Double d_boylam;
    String source;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView enlem=findViewById(R.id.enlem);
        final TextView boylam=findViewById(R.id.boylam);

        //map




        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        requestPermission();
                        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {


                                if(location!=null){

                                    // textView.setText(String.valueOf(location.getAltitude()));

                                    d_enlem =location.getLatitude();
                                    d_boylam=location.getLongitude();
                                    enlem.setText(String.valueOf(new DecimalFormat("##.###").format(d_enlem)));
                                    boylam.setText(String.valueOf(new DecimalFormat("##.###").format(d_boylam)));
                                    int tmp=(int)(d_boylam*1000);
                                    tmp=tmp%10;
                                    switch (tmp) {
                                        case 1 :
                                            source="http://etrafo.com/x/1.mp3";

                                            break;

                                        case 2 :
                                            source="http://etrafo.com/x/2.mp3";

                                            break;

                                        case 3 :
                                            source="http://etrafo.com/x/3.mp3";


                                            break;

                                        default :
                                            source="http://etrafo.com/x/2.mp3";

                                            break;
                                    }




                                }
                            }
                        });

                    }
                });
            }
        };




        timer = new Timer();

        timer.schedule(timerTask,1000,3000);


        musicView = (VusikView) findViewById(R.id.musicView);


        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(99);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mediaPlayer.isPlaying()) {

                    SeekBar seekBar = (SeekBar) v;
                    int playPosition = (mediaFileLength / 100) * seekBar.getProgress();
                    mediaPlayer.seekTo(playPosition);
                }
                return false;
            }
        });


        btn_play_pause = (ImageButton) findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

                @SuppressLint("StaticFieldLeak") AsyncTask<String, String, String> mp3Play = new AsyncTask<String, String, String>() {

                    @Override
                    protected void onPreExecute() {
                        mDialog.setMessage("Please wait!");
                        mDialog.show();
                    }

                    @Override
                    protected String doInBackground(String... params) {

                        try {
                            mediaPlayer.setDataSource(params[0]);
                            mediaPlayer.prepare();
                        } catch (Exception ex) {


                        }
                        return "";

                    }


                    @Override
                    protected void onPostExecute(String s) {

                        mediaFileLength = mediaPlayer.getDuration();
                        realtimeLenght = mediaFileLength;
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            btn_play_pause.setImageResource(R.drawable.ic_pause);
                        } else {

                            mediaPlayer.pause();
                            btn_play_pause.setImageResource(R.drawable.ic_play);

                        }

                        updateSeekBar();
                        mDialog.dismiss();

                    }
                };


                mp3Play.execute(source); //direck link
                musicView.start();

            }
        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);


    }

    private void updateSeekBar() {

        seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLength) * 100));
        if (mediaPlayer.isPlaying()) {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    realtimeLenght = 1000; //declare 1 seconds

                }

            };
            handler.postDelayed(updater, 1000);
        }

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play_pause.setImageResource(R.drawable.ic_play);
        musicView.stopNotesFall();
    }



    //konum güncellenmesi

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }
}
