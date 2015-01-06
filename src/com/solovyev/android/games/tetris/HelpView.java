/*
 * $Id: HelpView.java,v 1.1 2010/02/02 01:12:25 solovam Exp $
 */
package com.solovyev.android.games.tetris;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;


public class HelpView extends WebView
{
    private static final String HELP_FILE_ASSET_URL = "file:///android_asset/help.html";

    public HelpView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        loadUrl(HELP_FILE_ASSET_URL);
    }
}
