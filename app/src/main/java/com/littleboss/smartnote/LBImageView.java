package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.littleboss.smartnote.Utils.ImageUtils;

import java.io.FileNotFoundException;

public class LBImageView extends FrameLayout implements LBAbstractView {
    protected Context context;
    protected LayoutInflater inflater;
    protected ImageView imageView;
    protected View blankView;
    protected String filePath;
    protected int SCREEN_WIDTH;
    protected int SCREEN_HEIGHT;
    protected Bitmap image;
    protected LBClickListener clickListener;
    protected int width;
    protected int height;
    protected int resizeFlag;

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

//    不需要的函数
//    public void imageResize(String ratio);
//    public void imageResize(final int size, String flag);

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
        class ResizeDialog extends FrameLayout {

            private EditText nWidth, nHeight;
            private CheckBox keepratio;
            private boolean by_width, by_height;

            /**
             * 根据keepratio, by_width, by_height,
             * 从old_width和old_height推出new_width, new_height
             * 并确保它们符合数据范围约定：width<=SCREEN_WIDTH, height<=SCREEN_HEIGHT
             * */
            private Pair<Integer, Integer> getNewShape(int old_width, int old_height) {


                /**
                 * 若输入不为空，则按照EditText.inputType="number", .digits="0-9"约定
                 * 输入必为合法非负整数，可以放心调用parseInt
                 * 若输入为空，则视作保持原有尺寸
                 * */
                try {
                    /*
                    获得新长宽数据，
                    若新长/宽为空，令更新值无效，new_xxx=old_xxx
                    若新长/宽不为空，则输入为非负整数
                        若为0，令更新值无效，new_xxx=old_xxx
                        否则new_xxx有效
                    */
                    int new_width, new_height;

                    if(nWidth.getText().length()>0) {
                        new_width = Integer.parseInt(nWidth.getText().toString());
                        if(new_width==0) new_width = old_width;
                    } else {
                        new_width = old_width;
                    }

                    if(nHeight.getText().length()>0) {
                        new_height = Integer.parseInt(nHeight.getText().toString());
                        if(new_height==0) new_height = old_height;
                    } else {
                        new_height = old_height;
                    }

                    /*若不保持ratio，不修正new_width, new_height*/
                    if(!by_height && !by_width) {
                        // do nothing
                    }
                    /*保持ratio by height，按照new_height修正new_width*/
                    else if (by_height) {
                        float ratio = (float)(old_width) / old_height;
                        new_width = (int)(ratio * new_height);
                    }
                    /*保持ratio by width，按照new_width修正new_height*/
                    else {
                        float ratio = (float)(old_height) / old_width;
                        new_height = (int)(ratio * new_width);
                    }

                    if(new_height > SCREEN_HEIGHT) new_height = SCREEN_HEIGHT;
                    if(new_height > SCREEN_WIDTH) new_height = SCREEN_WIDTH;
                    return new Pair(new_width, new_height);
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                    return new Pair<Integer, Integer>(old_width, old_height);
                }
            }

            /**
             * 构造函数
             * 除了渲染xml以外，还需要绑定View对象，
             * 并设定监听器，
             * 根据CheckBox是否勾选，和EditText的编辑行为，更改by_width, by_height
             * */
            public ResizeDialog(@NonNull Context context) {
                super(context);

                LayoutInflater inflater = LayoutInflater.from(context);
                inflater.inflate(R.layout.resize_dialog, this);

                nWidth = findViewById(R.id.nWidth_input);
                nHeight = findViewById(R.id.nHeight_input);
                keepratio = findViewById(R.id.keep_ratio_checkbox);

                by_width = false;
                by_height = false;

                nWidth.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if(keepratio.isChecked()==true) {
                            by_width = true;
                            by_height = false;
                        }
                        return true;
                    }
                });
                nHeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if(keepratio.isChecked()==true) {
                            by_width = true;
                            by_height = false;
                        }
                        return true;
                    }
                });

                keepratio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if(checked) {
                            if(nHeight.getText().length()>0) {
                                by_height = true;
                                by_width = false;
                            } else if (nWidth.getText().length()>0) {
                                by_height = false;
                                by_width = true;
                            }
                        } else {
                            by_height = false;
                            by_width = false;
                        }
                    }
                });
            }
        }

        /**
         * 修改resize的互动方式
         * 提供两个输入框：高、宽
         * 提供选项：保持纵横比
         * 以及取消/保存
         *
         * 如果保存，则调用ImageUtils.resizeImage(this.Image, nWidth, nHeight)
         * */

        /**
         * 构造对话框：标题、选项栏、保存或退出选项
         * 为保存选项注册监听器：保存更改并退出
         * 为退出选项注册监听器：退出
         * */
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("调整大小");

        ResizeDialog resize_dialog = new ResizeDialog(context);
        builder.setView(resize_dialog);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                /**
                 * 从resize_dialog掌握的输入和现有图片长宽计算出新的长宽，
                 * 将bitmap交给ImageUtils做resize，
                 * setImage()设置图片
                 * */
                Pair<Integer, Integer> newShape = resize_dialog.getNewShape(image.getWidth(), image.getHeight());
                Bitmap newImage = ImageUtils.resizeImage(image, newShape.first, newShape.second);
                setImage(newImage);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.giveup, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

//    不需要的函数：
//    public void imageResizeWithScaleDialog();
//    public void chooseDialog(final String choice);

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
