package com.littleboss.smartnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.littleboss.smartnote.Utils.ImageUtils;


public class LBImageView extends FrameLayout implements LBAbstractView {
    protected NoteEditActivity context;
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
    protected boolean isFirst = true;

    public LBImageView(Context context) {
        this(context, null);
    }

    public LBImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.width = -1;
        this.height = -1;    // initialize width and height
        this.context = (NoteEditActivity) context;
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
        parseImageSize(filePath);
        this.setContent(this.filePath);
    }

    public void init() {
        resizeFlag = 0;
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
                if(clickListener != null)
                    clickListener.onContentLongClick(view, LBImageView.this);
                return false;
            }
        });
    }

    public void setContent(String filePath) {
        // get image
        this.filePath = filePath;
        getImage();

        // bind image to View
        if(isFirst) {
            setImage(this.image);
            return;
        }

        // 获取图片失败
        if(this.image == null)
            return;

        // 防止图片过大
        if(this.height > this.width && this.height > 1000) {
            float ratio = (float)(this.width) / this.height;
            this.height = 1000;
            this.width = (int)(ratio * this.height);
        }

        // 更改大小适应this.width, this.height
        Bitmap mImage = ImageUtils.resizeImage(this.image, this.width, this.height);
        getImageProperties(mImage);
        setImage(mImage);
        this.image = mImage;
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
            //e.printStackTrace();
            Log.i("error getImage() : ", e.toString());
        }
    }

    public void setImage(Bitmap mImage) {
        if(mImage == null) {
            Toast.makeText(this.context, "No image!", Toast.LENGTH_SHORT);
            return;
        }

        // 确保进来的图片一开始就不超宽度
        if(mImage.getWidth() >= SCREEN_WIDTH) {
            // 原始大小
            int old_width = mImage.getWidth();
            int old_height = mImage.getHeight();

            // 超出比例
            double width_exceed_rate = (double) (old_width) / SCREEN_WIDTH;

            // 确保超出比例更大的缩小到屏幕范围内（严格小于屏幕长宽）
            // 同时保证图片比例不失真
            // 所以同时除以宽度的超出比例
            int newWidth = (int)(old_width / width_exceed_rate) - 1;
            int newHeight = (int)(old_height / width_exceed_rate) - 1;
            mImage = ImageUtils.resizeImage(mImage, newWidth, newHeight);
        }

        this.image = mImage;
        this.imageView.setImageBitmap(mImage);
        getImageProperties(mImage);
    }

    public void parseImageSize(String str) {
        /**
         * @Author: Buzz Kim
         * @Date: 31/10/2018 8:05 PM
         * @param str
         * @Description: for parse the size of source image
         *
         */
        // FIXME: 2018/11/13 Added if condition for test
        if(str != null) {
            String[] buf = str.split("\\+");
            this.filePath = buf[0];
            if (buf.length > 1) {
                this.width = Integer.parseInt(buf[1]);
                this.height = Integer.parseInt(buf[2]);
                this.isFirst = false;
            }
        }
    }

    public void imageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("图片菜单");
        final String[] dialogItems = {"修改尺寸比例", "删除","上移","下移"};
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
                    case 2:
                        clickListener.moveUp(LBImageView.this);
                        break;
                    case 3:
                        clickListener.moveDown(LBImageView.this);
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

            final private EditText nWidth, nHeight;
            private CheckBox keepratio;
            private TextView note;
            final private double original_ratio;

            /* magic number:
                last_edit==0 => last edit on width
                last_edit==1 => last edit on height
            */
            int last_edit;

            /*
            * 更改输入框可能是由于勾选checkbox导致
            * 此时text watcher应当忽略此更改
            * 设定此更改锁，达到互锁的目的
            * */
            boolean adjustment_clear;
            boolean text_has_changed_when_unchecked;

            /**
             * 根据keepratio, by_width, by_height,
             * 从old_width和old_height推出new_width, new_height
             * 并确保它们符合数据范围约定：width<=SCREEN_WIDTH, height<=SCREEN_HEIGHT
             * */
            private Pair<Integer, Integer> getNewShape() {
                int new_width = Integer.parseInt(nWidth.getText().toString());
                int new_height = Integer.parseInt(nHeight.getText().toString());
                return new Pair<Integer, Integer>(new_width, new_height);
            }

            /**
             * 构造函数
             * 对话需要原先的宽和高：original_width, original_height, 以求得原有的长宽比
             * 浅状态：状态即显示
             * 除了渲染xml以外，还需要绑定View对象，
             * 并设定监听器，
             * */
            public ResizeDialog(@NonNull Context context, int original_width, int original_height) {
                super(context);

                // 渲染xml
                LayoutInflater inflater = LayoutInflater.from(context);
                inflater.inflate(R.layout.resize_dialog, this);

                // 绑定View对象
                nWidth = findViewById(R.id.nWidth_input);
                nHeight = findViewById(R.id.nHeight_input);
                keepratio = findViewById(R.id.keep_ratio_checkbox);
                note = findViewById(R.id.notify_limit);
                String limit_notify = "max width = " + Integer.toString(SCREEN_WIDTH) + ", max height " + Integer.toString(SCREEN_HEIGHT);
                note.setText(limit_notify);

                // 浅状态初始化：与初态相同；设最新更改项为width
                nWidth.setText(Integer.toString(original_width));
                nHeight.setText(Integer.toString(original_height));
                original_ratio = (double)(original_width)/original_height;

                last_edit = 0;
                keepratio.setChecked(true);
                adjustment_clear = true;
                text_has_changed_when_unchecked = false;

                // 浅状态维护：在用户动作中保持nWidth
                // 1. 最后更改对话框
                // 2. 最后更改CheckBox

                // [1]
                nWidth.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        last_edit = 0;
                        if(keepratio.isChecked()==true && adjustment_clear==true) {
                            adjustment_clear = false;
                            // do adjustment
                            // 用户将输入框更改为合法数字
                            try {
                                int new_width = Integer.parseInt(nWidth.getText().toString());
                                int new_height = (int)(new_width / original_ratio);
                                nHeight.setText(Integer.toString(new_height));
                            }
                            // 用户将输入框更改为非法数字
                            catch (NumberFormatException e) {
//                                nWidth.setText(Integer.toString(original_width));
//                                nHeight.setText(Integer.toString(original_height));
                            }
                            adjustment_clear = true;
                        } else {
                            text_has_changed_when_unchecked = true;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable a) {}
                });
                nHeight.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        last_edit = 1;
                        if(keepratio.isChecked()==true && adjustment_clear==true) {
                            adjustment_clear = false;
                            // do adjustment
                            // 用户将输入框更改为合法数字
                            try {
                                int new_height = Integer.parseInt(nHeight.getText().toString());
                                int new_width = (int)(new_height * original_ratio);
                                nWidth.setText(Integer.toString(new_width));
                            }
                            // 用户将输入框更改为非法数字
                            catch (NumberFormatException e) {
//                                nWidth.setText(Integer.toString(original_width));
//                                nHeight.setText(Integer.toString(original_height));
                            }
                            adjustment_clear = true;
                        } else {
                            text_has_changed_when_unchecked = true;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                // [2]
                keepratio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if(checked && text_has_changed_when_unchecked) {
                            // 假设切换到keep ratio之前的长宽不合比例：视作未经调整的

                            adjustment_clear = false;
                            int new_width=0,new_height=0;
                            try {
                                new_width = Integer.parseInt(nWidth.getText().toString());
                                new_height = Integer.parseInt(nHeight.getText().toString());
                                if(last_edit==0) {
                                    new_height = (int)(new_width / original_ratio);
                                } else {
                                    new_width = (int)(new_height * original_ratio);
                                }
                                nWidth.setText(Integer.toString(new_width));
                                nHeight.setText(Integer.toString(new_height));
                            }
                            catch (NumberFormatException ne){
                            }
                            adjustment_clear = true;
                            text_has_changed_when_unchecked = false;
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

        ResizeDialog resize_dialog = new ResizeDialog(context, image.getWidth(), image.getHeight());
        builder.setView(resize_dialog);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                /**
                 * 从resize_dialog掌握的输入和现有图片长宽计算出新的长宽，
                 * 将bitmap交给ImageUtils做resize，
                 * setImage()设置图片
                 * */
                Pair<Integer, Integer> newShape = resize_dialog.getNewShape();
                if(newShape.first >= SCREEN_WIDTH||newShape.second >= SCREEN_HEIGHT) {
                    Toast.makeText(context, "invalid size", Toast.LENGTH_LONG);
                } else {
                    Bitmap newImage = ImageUtils.resizeImage(image, newShape.first, newShape.second);
                    setImage(newImage);
                }
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

//    不需要的函数：已经删除
//    public void imageResizeWithScaleDialog();
//    public void chooseDialog(final String choice);

    private void removeMyself() {
        /**
         * HOLLY SHIT HERE!
         * HOLY SHIT!
         * */
        ((ViewManager)this.getParent()).removeView(this);
    }


    public void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("删除图片");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeMyself();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }


    @Override
    public String toDataString() {
        return "<image>"+ this.filePath + "+" + this.width + "+" + this.height +"</image>";
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
