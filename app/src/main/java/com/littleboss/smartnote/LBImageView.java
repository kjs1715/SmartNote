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
    protected int screenWidth;
    protected int screenHeight;
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
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
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


        OnClickListener onContentClickListener=(View view)->{
            if(clickListener != null)
                clickListener.onContentClick(view, LBImageView.this);
        };
        OnClickListener onBlankClickListener=(View view)->{
            if(clickListener != null)
                clickListener.onBlankViewClick(view, LBImageView.this);
        };
        OnLongClickListener onLongClickListener=(View view)->{
            if(clickListener != null)
                clickListener.onContentLongClick(view, LBImageView.this);
            return false;
        };

        this.imageView.setOnClickListener(onContentClickListener);
        this.imageView.setOnLongClickListener(onLongClickListener);

        this.blankView.setOnClickListener(onBlankClickListener);
        this.blankView.setOnLongClickListener(onLongClickListener);
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
            this.image = BitmapFactory.decodeFile(this.filePath);
        } catch (Exception e) {
            Log.i("error getImage() : ", e.toString());
        }
    }

    public void setImage(Bitmap mImage) {
        if(mImage == null) {
            Toast.makeText(this.context, "No image!", Toast.LENGTH_SHORT);
            return;
        }

        // 确保进来的图片一开始就不超宽度
        if(mImage.getWidth() >= screenWidth) {
            // 原始大小
            int oldWidth = mImage.getWidth();
            int oldHeight = mImage.getHeight();

            // 超出比例
            double widthExceedRate = (double) (oldWidth) / screenWidth;

            // 确保超出比例更大的缩小到屏幕范围内（严格小于屏幕长宽）
            // 同时保证图片比例不失真
            // 所以同时除以宽度的超出比例
            int newWidth = (int)(oldWidth / widthExceedRate) - 1;
            int newHeight = (int)(oldHeight / widthExceedRate) - 1;
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

            private final EditText nWidth;
            private final EditText nHeight;
            private CheckBox keepratio;
            private TextView note;
            final private double originalRatio;

            /* magic number:
                last_edit==0 => last edit on width
                last_edit==1 => last edit on height
            */
            int lastEdit;

            /*
            * 更改输入框可能是由于勾选checkbox导致
            * 此时text watcher应当忽略此更改
            * 设定此更改锁，达到互锁的目的
            * */
            boolean adjustmentClear;
            boolean textHasChangedWhenUnchecked;

            /**
             * 根据keepratio, by_width, by_height,
             * 从old_width和old_height推出new_width, new_height
             * 并确保它们符合数据范围约定：width<=SCREEN_WIDTH, height<=SCREEN_HEIGHT
             * */
            private Pair<Integer, Integer> getNewShape() {
                int newWidth = Integer.parseInt(nWidth.getText().toString());
                int newHeight = Integer.parseInt(nHeight.getText().toString());
                return new Pair<>(newWidth, newHeight);
            }

            /**
             * 构造函数
             * 对话需要原先的宽和高：original_width, original_height, 以求得原有的长宽比
             * 浅状态：状态即显示
             * 除了渲染xml以外，还需要绑定View对象，
             * 并设定监听器，
             * */
            public ResizeDialog(@NonNull Context context, int originalWidth, int originalHeight) {
                super(context);

                // 渲染xml
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                layoutInflater.inflate(R.layout.resize_dialog, this);

                // 绑定View对象
                nWidth = findViewById(R.id.nWidth_input);
                nHeight = findViewById(R.id.nHeight_input);
                keepratio = findViewById(R.id.keep_ratio_checkbox);
                note = findViewById(R.id.notify_limit);
                String limitNotify = "max width = " + Integer.toString(screenWidth) + ", max height " + Integer.toString(screenHeight);
                note.setText(limitNotify);

                // 浅状态初始化：与初态相同；设最新更改项为width
                nWidth.setText(Integer.toString(originalWidth));
                nHeight.setText(Integer.toString(originalHeight));
                originalRatio = (double)(originalWidth)/originalHeight;

                lastEdit = 0;
                keepratio.setChecked(true);
                adjustmentClear = true;
                textHasChangedWhenUnchecked = false;

                // 浅状态维护：在用户动作中保持nWidth
                // 1. 最后更改对话框
                // 2. 最后更改CheckBox

                // [1]
                nWidth.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // nothing to do
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        lastEdit = 0;
                        if(keepratio.isChecked() && adjustmentClear) {
                            adjustmentClear = false;
                            // do adjustment
                            // 用户将输入框更改为合法数字
                            try {
                                int newWidth = Integer.parseInt(nWidth.getText().toString());
                                int newHeight = (int)(newWidth / originalRatio);
                                nHeight.setText(Integer.toString(newHeight));
                            }
                            // 用户将输入框更改为非法数字
                            catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            adjustmentClear = true;
                        } else {
                            textHasChangedWhenUnchecked = true;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable a) {   throw new UnsupportedOperationException();
                    } });
                nHeight.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {   throw new UnsupportedOperationException();
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        lastEdit = 1;
                        if(keepratio.isChecked() && adjustmentClear) {
                            adjustmentClear = false;
                            // do adjustment
                            // 用户将输入框更改为合法数字
                            try {
                                int newHeight = Integer.parseInt(nHeight.getText().toString());
                                int newWidth = (int)(newHeight * originalRatio);
                                nWidth.setText(Integer.toString(newWidth));
                            }
                            // 用户将输入框更改为非法数字
                            catch (NumberFormatException e) {
                                //do nothing
                            }
                            adjustmentClear = true;
                        } else {
                            textHasChangedWhenUnchecked = true;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //nothing to do
                    }
                });

                // [2]
                keepratio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if(checked && textHasChangedWhenUnchecked) {
                            // 假设切换到keep ratio之前的长宽不合比例：视作未经调整的

                            adjustmentClear = false;
                            int newWidth;
                            int newHeight;
                            newWidth = Integer.parseInt(nWidth.getText().toString());
                            newHeight = Integer.parseInt(nHeight.getText().toString());
                            if(lastEdit==0) {
                                newHeight = (int)(newWidth / originalRatio);
                            } else {
                                newWidth = (int)(newHeight * originalRatio);
                            }
                            nWidth.setText(Integer.toString(newWidth));
                            nHeight.setText(Integer.toString(newHeight));
                            adjustmentClear = true;
                            textHasChangedWhenUnchecked = false;
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

        ResizeDialog resizeDialog = new ResizeDialog(context, image.getWidth(), image.getHeight());
        builder.setView(resizeDialog);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                /**
                 * 从resize_dialog掌握的输入和现有图片长宽计算出新的长宽，
                 * 将bitmap交给ImageUtils做resize，
                 * setImage()设置图片
                 * */
                Pair<Integer, Integer> newShape = resizeDialog.getNewShape();
                if(newShape.first >= screenWidth ||newShape.second >= screenHeight) {
                    Toast.makeText(context, "invalid size", Toast.LENGTH_LONG).show();
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
                //nothing to do
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
