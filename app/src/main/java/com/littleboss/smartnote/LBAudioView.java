package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 录音模块。
 * 单击播放按钮可进行播放，在播放中单击播放按钮可实现暂停。
 * 文本部分是一个内置的文本编辑可进行编辑。
 */
public class LBAudioView extends FrameLayout implements LBAbstractView {
    private ImageView playIcon;
    public EditText content;
    private String audioFilePath;
    private MediaPlayer mediaPlayer;
    private Activity activity;
    private View blankView;
    private LayoutInflater inflater;
    private LBClickListener clickListener;
    private void createPlayIconAndText() {
        playIcon = findViewById(R.id.playicon);
        content = findViewById(R.id.textFromAudio);
        blankView = findViewById(R.id.blank_view);
    }

    private void addPlayIconClickListener() {
        playIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(audioFilePath);
                    }
                    catch (Exception e) {
                        Log.i("error addPlayIconCli...", e.toString());
                    }
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.prepareAsync();
                }
                else {
                    mediaPlayer.stop();
                }
            }
        });
    }

    /**
     * 识别结束后的回调函数。
     * 这类函数的ui操作必须放在ui线程执行。
     *
     * @param text the text
     */
    public void setRecognizedText(final String text) {
        activity.runOnUiThread(()->{
                content.setText(text);
        });
    }

    public LBAudioView(Context context) {
        this(context,null);
    }
    public LBAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = (Activity)context;
        this.inflater = LayoutInflater.from(activity);
        inflater.inflate(R.layout.item_audioview,this);
        createPlayIconAndText();
        initGeneralListener();
    }

    public LBAudioView(String content,Context context) {
        this(context);
        this.setContent(content);
    }

    public LBAudioView(String audioFilePath,Context context,@Nullable Object dumb, boolean sTT) {
        this(context);
        this.audioFilePath=audioFilePath;
        this.activity=(Activity)context;
        createPlayIconAndText();
        addPlayIconClickListener();
        initGeneralListener();

        if (sTT)
            new MSSpeechRecognizer().getRecognizedText(audioFilePath, this);

    }

    public void initGeneralListener()
    {
        OnClickListener onBlankClickListener=(View view)->{
            if(clickListener != null)
                clickListener.onBlankViewClick(view, LBAudioView.this);
        };
        OnLongClickListener onLongClickListener=(View view)->{
            if(clickListener != null)
                clickListener.onContentLongClick(view, LBAudioView.this);
            return false;
        };
        playIcon.setOnLongClickListener(onLongClickListener);
        blankView.setOnClickListener( onBlankClickListener);
        blankView.setOnLongClickListener(onLongClickListener);
    }

    public void audioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("语音菜单");
        final String[] dialogItems = {"删除","上移","下移"};
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        deleteDialog();
                        break;
                    case 1:
                        clickListener.moveUp(LBAudioView.this);
                        break;
                    case 2:
                        clickListener.moveDown(LBAudioView.this);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void removeMyself() {
        ((ViewManager)this.getParent()).removeView(this);
    }


    public void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("删除语音");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeMyself();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("onClick 取消","");
            }
        });

        builder.show();
    }

    //根据content内容设置audioView的显示与动作
    @Override
    public void setContent(String content)
    {
        Pattern pattern=Pattern.compile("src=(.*)content=(.*)");
        Matcher matcher=pattern.matcher(content);
        if(matcher.find())
        {
            String filepath=matcher.group(1);
            String text=matcher.group(2);
            audioFilePath = filepath;
            this.content.setText(text);
        }
        addPlayIconClickListener();
    }

    @Override
    public String toDataString() {
        String replace = content.getText().toString();
        return "<audio>src="+audioFilePath+"content="+replace+"</audio>";
    }

    @Override
    public void setOnClickViewListener(LBClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.AUDIO;
    }

    @Override
    public String getFilePath() {
        return audioFilePath;
    }

    @Override
    public View getView() {
        return this;
    }
}
