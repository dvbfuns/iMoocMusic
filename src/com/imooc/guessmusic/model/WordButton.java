package com.imooc.guessmusic.model;

import android.widget.Button;

/**
 * ���ְ�ť
 * @author lijian
 *
 */
public class WordButton {

	public int mIndex;
	public boolean mIsVisible;
	public String mWordString;
	public Button mViewButton;
	
	public WordButton(){
		mIsVisible = true;
		mWordString = "";
	}
}
