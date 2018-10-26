package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.littleboss.smartnote.Utils.ImageUtils;

import java.io.FileNotFoundException;

public class LBImageView extends FrameLayout implements LBAbstractView {
    /**
     * @Author: Buzz Kim
     * @Date: 24/10/2018 7:13 PM
     * @param null
     * @Description: Usage: LBImageView lbImageView = new LBImageView("filepath");
     *
     */
    private Context context;
    private LayoutInflater inflater;
    private ImageView imageView;
    private View blankView;
    private String uriString;
    private Uri uri;
    private int SCREEN_WIDTH;
    private Bitmap image;
    private LBClickListener clickListener;

    public LBImageView(Context context) {
        this(context, null);
    }
    public LBImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.inflater.inflate(R.layout.item_imageview, this);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        SCREEN_WIDTH = dm.widthPixels;
        init();
    }

    public LBImageView(String uriString,Context context) {
        this(context);
        this.uriString = uriString;
        this.setContent(uriString);
    }

    public LBImageView(Uri uri,Context context) {
        this(context);
        this.uriString = uri.toString();
        this.uri = uri;
        this.setContent(uri);
    }

    public LBImageView(Uri uri, Context context, AttributeSet attrs) {
        this(context, attrs);
        this.uriString = uri.toString();
        this.uri = uri;
        this.setContent(uri);
    }

    public void init() {
        this.imageView = findViewById(R.id.imageView);
        this.blankView = findViewById(R.id.blank_view);
        this.imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onContentClick(view, LBImageView.this);
            }
        });
        this.imageView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clickListener != null)
                    clickListener.onContentLongClick(view, LBImageView.this);
                return false;
            }
        });
        this.blankView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null)
                    clickListener.onBlankViewClick(view, LBImageView.this);
            }
        });
        this.blankView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                imageDialog();
                if(clickListener != null)
                    clickListener.onContentLongClick(view, LBImageView.this);
                return false;
            }
        });
    }

    public void setContent(String uriString)
    {
        this.uriString=uriString;
        this.uri=Uri.parse(uriString);
        getImage();
        if(this.image==null)
            return;
        Bitmap mImage = ImageUtils.resizeImage(this.image, SCREEN_WIDTH * 16 / 25, SCREEN_WIDTH * 9 / 25);
        setImage(mImage);
    }

    public void setContent(Uri uri)
    {
        this.uriString=uri.toString();
        this.uri=uri;
        getImage();
        if(this.image==null)
            return;
        Bitmap mImage = ImageUtils.resizeImage(this.image, SCREEN_WIDTH * 16 / 25, SCREEN_WIDTH * 9 / 25);
        setImage(mImage);
    }

    public void getImage() {
        ContentResolver contentResolver=context.getContentResolver();
        try {
            this.image = BitmapFactory.decodeStream(contentResolver.openInputStream(this.uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setImage(Bitmap mImage) {
        this.imageView.setImageBitmap(mImage);
    }


    public void imageResize(String ratio) {
        String[] num = ratio.split(":");
        final int scaleSum = Integer.parseInt(num[0]) + Integer.parseInt(num[1]);;
        Bitmap mImage = ImageUtils.resizeImage(this.image, SCREEN_WIDTH * Integer.parseInt(num[0]) / scaleSum, SCREEN_WIDTH * Integer.parseInt(num[1]) / scaleSum);
        setImage(mImage);
    }

    public void imageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("图片菜单");
        final String[] dialogItems = {"修改图片尺寸比例", "删除图片"};
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        ResizeDialog();
                        break;
                    case 1:
                        deleteDialog();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    public void ResizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("选择尺寸");
        final String[] dialogItems = {"16:9", "4:3"};
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageResize(dialogItems[which]);
            }
        });
        builder.show();
    }

    public void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("删除图片");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "删除了图片", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("不了不了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"取消删除", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }



    @Override
    public String toDataString() {
        return "<image>"+ this.uriString +"</image>";
    }

    @Override
    public void setOnClickViewListener(LBClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.IMAGE;
    }

    @Override
    public String getFilePath() {
        return this.uriString;
    }

    @Override
    public View getView() {
        return this;
    }
}
