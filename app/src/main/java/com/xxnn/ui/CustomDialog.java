package com.xxnn.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.xxnn.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.xxnn.utils.Initiator.load;
import static com.xxnn.utils.ReflexUtil.iget_object_or_null;
import static com.xxnn.utils.ReflexUtil.invoke_virtual;
import static com.xxnn.utils.Utils.strcmp;
import static de.robv.android.xposed.XposedBridge.log;

public class CustomDialog {

    private static Class<?> clz_DialogUtil;
    private static Class<?> clz_CustomDialog;
    private static Method m_DialogUtil_a;
    @SuppressWarnings("deprecation")
    private static int THEME_LIGHT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
            ? android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
            : AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
    @SuppressWarnings("deprecation")
    private static int THEME_DARK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
            ? android.R.style.Theme_DeviceDefault_Dialog_Alert : AlertDialog.THEME_DEVICE_DEFAULT_DARK;
    private Dialog mDialog = null;
    private AlertDialog mFailsafeDialog = null;
    private AlertDialog.Builder mBuilder = null;
    private boolean failsafe = false;

    public static CustomDialog create(Context ctx) {
        try {
            if (clz_DialogUtil == null) {
                clz_DialogUtil = load("com/tencent/mobileqq/utils/DialogUtil");
            }
            if (clz_CustomDialog == null) {
                clz_CustomDialog = load("com/tencent/mobileqq/utils/QQCustomDialog");
                if (clz_CustomDialog == null) {
                    Class clz_Lite = load("com/dataline/activities/LiteActivity");
                    Field[] fs = clz_Lite.getDeclaredFields();
                    for (Field f : fs) {
                        if (Modifier.isPrivate(f.getModifiers()) && Dialog.class
                                .isAssignableFrom(f.getType())) {
                            clz_CustomDialog = f.getType();
                            break;
                        }
                    }
                }
            }
            if (m_DialogUtil_a == null) {
                Method tmpa = null, tmpb = null;
                for (Method m : clz_DialogUtil.getDeclaredMethods()) {
                    if (m.getReturnType().equals(clz_CustomDialog) && (Modifier
                            .isPublic(m.getModifiers()))) {
                        Class<?>[] argt = m.getParameterTypes();
                        if (argt.length != 2) {
                            continue;
                        }
                        if (argt[0].equals(Context.class) && argt[1].equals(int.class)) {
                            if (m.getName().equals("a")) {
                                m_DialogUtil_a = m;
                                break;
                            } else {
                                if (tmpa == null) {
                                    tmpa = m;
                                } else {
                                    tmpb = m;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (m_DialogUtil_a == null && tmpa != null) {
                    if (tmpb == null) {
                        m_DialogUtil_a = tmpa;
                    } else {
                        m_DialogUtil_a = (strcmp(tmpa.getName(), tmpb.getName()) > 0) ? tmpb : tmpa;
                    }
                }
            }
        } catch (Exception e) {
            log(e);
        }
        CustomDialog ref = new CustomDialog();
        try {
            ref.mDialog = (Dialog) m_DialogUtil_a.invoke(null, ctx, 230);
        } catch (Exception e) {
            log(e);
        }
        if (ref.mDialog == null) {
            ref.failsafe = true;
            ref.mBuilder = new AlertDialog.Builder(ctx, THEME_LIGHT);
            ref.mBuilder.setMessage("");
        }
        return ref;
    }

    public static int themeIdForDialog() {
        return THEME_LIGHT;
    }

    public static CustomDialog createFailsafe(Context ctx) {
        CustomDialog ref = new CustomDialog();
        ref.failsafe = true;
        ref.mBuilder = new AlertDialog.Builder(ctx, THEME_LIGHT);
        ref.mBuilder.setMessage("");
        return ref;
    }

    public CustomDialog setCancelable(boolean flag) {
        if (!failsafe) {
            mDialog.setCancelable(flag);
            if (flag) {
                mDialog.setCanceledOnTouchOutside(true);
            }
        } else {
            mBuilder.setCancelable(flag);
        }
        return this;
    }

    public CustomDialog setTitle(String title) {
        if (!failsafe) {
            try {
                invoke_virtual(mDialog, "setTitle", title, String.class);
            } catch (Exception e) {
                log(e);
            }
        } else {
            if (mFailsafeDialog == null) {
                mBuilder.setTitle(title);
            } else {
                mFailsafeDialog.setTitle(title);
            }
        }
        return this;
    }

    public CustomDialog setMessage(CharSequence msg) {
        if (!failsafe) {
            try {
                invoke_virtual(mDialog, "setMessage", msg, CharSequence.class);
            } catch (Exception e) {
                log(e);
            }
        } else {
            if (mFailsafeDialog == null) {
                mBuilder.setMessage(msg);
            } else {
                mFailsafeDialog.setMessage(msg);
            }
        }
        return this;
    }

    public Context getContext() {
        if (!failsafe) {
            return mDialog.getContext();
        } else {
            if (mFailsafeDialog == null) {
                return mBuilder.getContext();
            } else {
                return mFailsafeDialog.getContext();
            }
        }
    }

    public CustomDialog setView(View v) {
        if (!failsafe) {
            try {
                invoke_virtual(mDialog, "setView", v, View.class);
            } catch (Exception e) {
                log(e);
            }
        } else {
            if (mFailsafeDialog == null) {
                mBuilder.setView(v);
            } else {
                mFailsafeDialog.setView(v);
            }
        }
        return this;
    }

    @NonNull
    public CustomDialog setPositiveButton(int text,
                                          @Nullable DialogInterface.OnClickListener listener) {
        Context ctx;
        if (failsafe) {
            ctx = mBuilder.getContext();
        } else {
            ctx = mDialog.getContext();
        }
        return setPositiveButton(ctx.getString(text), listener);
    }

    @NonNull
    public CustomDialog setNegativeButton(int text,
                                          @Nullable DialogInterface.OnClickListener listener) {
        Context ctx;
        if (failsafe) {
            ctx = mBuilder.getContext();
        } else {
            ctx = mDialog.getContext();
        }
        return setNegativeButton(ctx.getString(text), listener);
    }

    @NonNull
    public CustomDialog ok() {
        setPositiveButton(android.R.string.ok, null);
        return this;
    }

    @NonNull
    public CustomDialog setPositiveButton(@NonNull String text,
                                          @Nullable DialogInterface.OnClickListener listener) {
        if (!failsafe) {
            if (text != null && listener == null) {
                listener = new Utils.DummyCallback();
            }
            try {
                invoke_virtual(mDialog, "setPositiveButton", text, listener, String.class,
                        DialogInterface.OnClickListener.class);
            } catch (Exception e) {
                log(e);
            }
        } else {
            mBuilder.setPositiveButton(text, listener);
        }
        return this;
    }

    @NonNull
    public CustomDialog setNeutralButton(@NonNull String text,
                                         @Nullable DialogInterface.OnClickListener listener) {
        if (!failsafe) {
            //They don't have a neutral button, sigh...
        } else {
            mBuilder.setNeutralButton(text, listener);
        }
        return this;
    }

    @NonNull
    public CustomDialog setNeutralButton(int text,
                                         @Nullable DialogInterface.OnClickListener listener) {
        if (!failsafe) {
            //They don't have a neutral button, sigh...
        } else {
            mBuilder.setNeutralButton(text, listener);
        }
        return this;
    }

    public CustomDialog setNegativeButton(String text, DialogInterface.OnClickListener listener) {
        if (!failsafe) {
            if (text != null && listener == null) {
                listener = new Utils.DummyCallback();
            }
            try {
                invoke_virtual(mDialog, "setNegativeButton", text, listener, String.class,
                        DialogInterface.OnClickListener.class);
            } catch (Exception e) {
                log(e);
            }
        } else {
            mBuilder.setNegativeButton(text, listener);
        }
        return this;
    }

    public Dialog create() {
        if (!failsafe) {
            return mDialog;
        } else {
            if (mFailsafeDialog == null) {
                mFailsafeDialog = mBuilder.create();
            }
            return mFailsafeDialog;
        }
    }

    public Dialog show() {
        if (!failsafe) {
            mDialog.show();
            return mDialog;
        } else {
            if (mFailsafeDialog == null) {
                mFailsafeDialog = mBuilder.create();
            }
            mFailsafeDialog.show();
            return mFailsafeDialog;
        }
    }

    public void dismiss() {
        if (!failsafe) {
            mDialog.dismiss();
        } else {
            if (mFailsafeDialog != null) {
                mFailsafeDialog.dismiss();
            }
        }
    }

    public boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        }
        if (mFailsafeDialog != null) {
            return mFailsafeDialog.isShowing();
        }
        return false;
    }

    @Nullable
    public TextView getMessageTextView() {
        if (!failsafe) {
            return (TextView) iget_object_or_null(mDialog, "text");
        } else {
            return null;
        }
    }

}
