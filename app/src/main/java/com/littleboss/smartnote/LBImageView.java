package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.littleboss.smartnote.Utils.ImageUtils;

import java.io.FileNotFoundException;

public class LBImageView extends FrameLayout implements LBAbstractView {
    private Context context;
    private LayoutInflater inflater;
    private ImageView imageView;
    private View blankView;
    private String filePath;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private Bitmap image;
    private LBClickListener clickListener;
    private int width;
    private int height;
    private int resizeFlag;

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
        SCREEN_HEIGHT = dm.heightPixels;
        init();
    }

    public LBImageView(String filePath,Context context) {
        this(context);
        this.filePath = filePath;
        this.setContent(filePath);
    }

    public void init() {
        this.imageView = findViewById(R.id.imageView);
        resizeFlag = 0;
        this.imageView = (ImageView) findViewById(R.id.imageView);
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

    public void setContent(String filePath)
    {
        this.filePath=filePath;
        getImage();
        if(this.image==null)
            return;
//        Bitmap mImage = ImageUtils.resizeImage(this.image, SCREEN_WIDTH * 16 / 25, SCREEN_WIDTH * 9 / 25);
        setImage(this.image);
        getImageProperties(this.image);
    }

    public void getImageProperties(Bitmap mImage) {
        if(mImage == null) {
            return;
        }
        this.width = mImage.getWidth();
        this.height = mImage.getHeight();
    }

    public void getImage() {
        try {
            System.out.println(filePath);
            this.image = BitmapFactory.decodeFile(this.filePath);
            System.out.println("Successfully decode");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImage(Bitmap mImage) {
        if(mImage == null) {
            Toast.makeText(this.context, "No image!", Toast.LENGTH_SHORT);
            return ;
        }
        this.imageView.setImageBitmap(mImage);
        getImageProperties(mImage);
    }


    public void imageResize(String ratio) {
        /**
         * @Author: Buzz Kim
         * @Date: 25/10/2018 10:00 PM
         * @param ratio
         * @Description: Resize the image with a new ratio
         *
         */
        String[] num = ratio.split(":");
        final int scaleSum = Integer.parseInt(num[0]) + Integer.parseInt(num[1]);;
        Bitmap mImage = ImageUtils.resizeImage(this.image, SCREEN_WIDTH * Integer.parseInt(num[0]) / scaleSum, SCREEN_WIDTH * Integer.parseInt(num[1]) / scaleSum);
        setImage(mImage);
    }

    public void imageResize(final int size, String flag) {
        /**
         * @Author: Buzz Kim
         * @Date: 25/10/2018 10:00 PM
         * @param size
         * @param flag
         * @Description: Resize the image with the size provided by the user
         *
         */
        Bitmap mImage = null;
        if(flag.equals("宽度")) {
            if(size <= 0 || size > SCREEN_WIDTH)
            {
                Toast.makeText(this.context,"超过了宽度范围，请重新输入大小！", Toast.LENGTH_SHORT).show();
                return ;
            }
            float ratio = (float) size / this.width * this.height;
            mImage = ImageUtils.resizeImage(this.image, size, (int) ratio);
        } else {
            if(size <= 0 || size > SCREEN_HEIGHT)
            {
                Toast.makeText(this.context,"超过了长度范围，请重新输入大小！", Toast.LENGTH_SHORT).show();
                return ;
            }
            float ratio = (float) size / this.height * this.width;
            // TODO: 26/10/2018 A little problem with ratio of width and height to fix 
            if(ratio > SCREEN_WIDTH) {                            // If the size of height is too big, it would change the ratio of width and height, judge with resizeFlag
            }
            mImage = ImageUtils.resizeImage(this.image, (int) ratio, size);
        }
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
        final String[] dialogItems = {"保持纵横比", "16:9", "4:3"};
        builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    imageResizeWithScaleDialog();
                    return ;
                }
                imageResize(dialogItems[which]);
            }
        });
        builder.show();
    }

    public void imageResizeWithScaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        final String[] choice = {"高度", "宽度"};
        builder.setSingleChoiceItems(choice, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooseDialog(choice[which]);
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void chooseDialog(final String choice) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.context);
        LayoutInflater factory = LayoutInflater.from(this.context);
        final View view = factory.inflate(R.layout.item_inputsize, null);
        final EditText size = (EditText) view.findViewById(R.id.et);
        mBuilder.setTitle("请输入尺寸");
        mBuilder.setView(view);
        if(choice.equals("长度")) {
            size.setHint("请输入高度大小：1 ~ " + SCREEN_HEIGHT);
        } else {
            size.setHint("请输入宽度大小：1 ~ " + SCREEN_WIDTH);
        }
        mBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int mWhich) {
                if(size.getText() == null || size.getText().toString().equals(""))
                    return ;
                int num = Integer.parseInt(size.getText().toString());
                imageResize(num, choice);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mBuilder.show();
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
        return "<image>"+ this.filePath +"</image>";
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
        return this.filePath;
    }

    @Override
    public View getView() {
        return this;
    }
}
