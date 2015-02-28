package com.imooc.guessmusic.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.imooc.guessmusic.R;
import com.imooc.guessmusic.data.Const;
import com.imooc.guessmusic.model.IWordButtonClickListener;
import com.imooc.guessmusic.model.Song;
import com.imooc.guessmusic.model.WordButton;
import com.imooc.guessmusic.myui.MyGridView;
import com.imooc.guessmusic.util.MyLog;
import com.imooc.guessmusic.util.Util;

public class MainActivity extends Activity 
	implements IWordButtonClickListener {

	public static final String TAG = "MainActivity";
	
	
	public static final int STATUS_ANSWER_RIGHT = 1;
	public static final int STATUS_ANSWER_WRONG = 2;
	public static final int STATUS_ANSWER_LACK = 3;
	
	public static final int SPASH_TIMES = 6;
	
	// ��Ƭ��ض���
	private Animation mPanAnim;
	private LinearInterpolator mPanLin;
	
	private Animation mBarInAnim;
	private LinearInterpolator mBarInLin;
	
	private Animation mBarOutAnim;
	private LinearInterpolator mBarOutLin;
	
	// ��Ƭ�ؼ�
	private ImageView mViewPan;
	// ���˿ؼ�
	private ImageView mViewPanBar;
	
	// Play �����¼�
	private ImageButton mBtnPlayStart;
	
	//���ؽ���
	private View mPassView;
	
	// ��ǰ�����Ƿ���������
	private boolean mIsRunning = false;
	
	// ���ֿ�����
	private ArrayList<WordButton> mAllWords;
	
	private ArrayList<WordButton> mBtnSelectWords;
	
	private MyGridView mMyGridView;
	
	// ��ѡ�����ֿ�UI����
	private LinearLayout mViewWordsContainer;
	
	// ��ǰ�ĸ���
	private Song mCurrentSong;
	
	// ��ǰ�ص�����
	private int mCurrentStageIndex = -1;
	
	//��ǰ�������
	private int mCurrentCoins = Const.TOTAL_COINS;
	
	//���view
	private TextView mViewCurrentCoins;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// ��ʼ���ؼ�
		mViewPan = (ImageView)findViewById(R.id.imageView1);
		mViewPanBar = (ImageView)findViewById(R.id.imageView2);
		
		mMyGridView = (MyGridView)findViewById(R.id.gridview);
		
		mViewCurrentCoins = (TextView)findViewById(R.id.txt_bar_coins);
		mViewCurrentCoins.setText(mCurrentCoins + "");
		
		
		// ע�����
		mMyGridView.registOnWordButtonClick(this);
		
		mViewWordsContainer = (LinearLayout)findViewById(R.id.word_select_container);
		
		// ��ʼ������
		mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mPanLin = new LinearInterpolator();
		mPanAnim.setInterpolator(mPanLin);
		mPanAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	// ���������˳�����
            	mViewPanBar.setAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
		
		mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
		mBarInLin = new LinearInterpolator();
		mBarInAnim.setFillAfter(true);
		mBarInAnim.setInterpolator(mBarInLin);
		mBarInAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	// ��ʼ��Ƭ����
            	mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
		
		mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
		mBarOutLin = new LinearInterpolator();
		mBarOutAnim.setFillAfter(true);
		mBarOutAnim.setInterpolator(mBarOutLin);
		mBarOutAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	// ���׶����������
            	mIsRunning = false;
            	mBtnPlayStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
		
		mBtnPlayStart = (ImageButton)findViewById(R.id.btn_play_start);
		mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				handlePlayButton();
			}
		});
		
		// ��ʼ����Ϸ����
		initCurrentStageData();
		handleDeleteWord();
		handleTipAnswer();
	}
	
	@Override
	public void onWordButtonClick(WordButton wordButton) {
//		Toast.makeText(this, wordButton.mIndex + "", Toast.LENGTH_SHORT).show();
		setSelectWord( wordButton );
		
		//��ô�״̬
		int checkResult = checkTheAnswer();
		
		//����
		if( checkResult == STATUS_ANSWER_RIGHT ){
			//�����Ӧ����������
			handlePassEvent();
			
		} else if ( checkResult == STATUS_ANSWER_WRONG ){
			//���д�����ʾ����˸������ʾ�û�
			sparkTheWords();
			
		} else if ( checkResult == STATUS_ANSWER_LACK ){
			//��ȱʧ
			for( int i = 0; i < mBtnSelectWords.size(); ++i ){
				mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
			
		}
	}
	
	/**
	 * ��������¼�
	 */
	private void handlePassEvent(){
		mPassView = (LinearLayout)this.findViewById(R.id.pass_view);
		mPassView.setVisibility(View.VISIBLE);
		
	}
	
	private void clearTheAnswer( WordButton wordButton ){
		wordButton.mViewButton.setText("");
		wordButton.mWordString = "";
		wordButton.mIsVisible = false;
		
		//���ô�ѡ��Ŀɼ���
		setButtonVisiable( mAllWords.get(wordButton.mIndex), View.VISIBLE );
	}
	
	/**
	 * ���ô�ѡ���ֿ��Ƿ�ɼ�
	 * @param button
	 * @param visibility
	 */
	private void setButtonVisiable( WordButton button, int visibility ){
		button.mViewButton.setVisibility( visibility );
		button.mIsVisible = (visibility == View.VISIBLE) ? true : false;
		
		//Log
		MyLog.d(TAG,button.mIsVisible+"");
	}
	
	
	
	/**
	 * ���ô�
	 * @param wordButton
	 */
	private void setSelectWord( WordButton wordButton ){
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			if( mBtnSelectWords.get(i).mWordString.length() == 0 ){
				//���ô����ֿ����ݼ��ɼ���
				mBtnSelectWords.get( i ).mViewButton.setText( wordButton.mWordString);
				mBtnSelectWords.get( i ).mIsVisible = true;
				mBtnSelectWords.get(i).mWordString = wordButton.mWordString;
				
				//��¼����
				mBtnSelectWords.get(i).mIndex = wordButton.mIndex;
				
				//Log....
				MyLog.d(TAG, mBtnSelectWords.get(i).mIndex+"");
				//���ô�ѡ��ɼ���
				setButtonVisiable( wordButton, View.INVISIBLE);
				break;
			}
		}
	}
	
    /**
     * ����Բ���м�Ĳ��Ű�ť�����ǿ�ʼ��������
     */
	private void handlePlayButton() {
		if (mViewPanBar != null) {
			if (!mIsRunning) {
				mIsRunning = true;
				
				// ��ʼ���˽��붯��
				mViewPanBar.startAnimation(mBarInAnim);
				mBtnPlayStart.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
    public void onPause() {
        mViewPan.clearAnimation();
        
        super.onPause();
    }
	
	private Song loadStageSongInfo(int stageIndex) {
		Song song = new Song();
		
		String[] stage = Const.SONG_INFO[stageIndex];
		song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
		song.setSongName(stage[Const.INDEX_SONG_NAME]);
		
		return song;
	}
	
	private void initCurrentStageData() {
		// ��ȡ��ǰ�صĸ�����Ϣ
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);
		// ��ʼ����ѡ���
		mBtnSelectWords = initWordSelect();
		
		LayoutParams params = new LayoutParams(140, 140);
		
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(
					mBtnSelectWords.get(i).mViewButton,
					params);
		}
		
		// �������
		mAllWords = initAllWord();
		// ��������- MyGridView
		mMyGridView.updateData(mAllWords);
	}
	
	/**
	 * ��ʼ����ѡ���ֿ�
	 */
	private ArrayList<WordButton> initAllWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();
		
		// ������д�ѡ����
	    String[] words = generateWords();
		
		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			WordButton button = new WordButton();
			
			button.mWordString = words[i];
			
			data.add(button);
		}
		
		return data;
	}
	
	/**
	 * ��ʼ����ѡ�����ֿ�
	 * 
	 * @return
	 */
	private ArrayList<WordButton> initWordSelect() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();
		
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			View view = Util.getView(MainActivity.this, R.layout.self_ui_gridview_item);
			
			final WordButton holder = new WordButton();
			
			holder.mViewButton = (Button)view.findViewById(R.id.item_btn);
			holder.mViewButton.setTextColor(Color.WHITE);
			holder.mViewButton.setText("");
			holder.mIsVisible = false;
			
			holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
			
			holder.mViewButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					clearTheAnswer( holder );
				}
			});
			
			data.add(holder);
		}
		
		return data;
	}
	
	//������������
	private String[] generateWords(){
		
		Random random = new Random();
		
		String[] words = new String[MyGridView.COUNTS_WORDS];
		
		//�������
		for( int i = 0; i < mCurrentSong.getNameLength(); ++i ){
			words[i] = mCurrentSong.getNameCharacters()[i] + "";
			
		}
		
		//��ȡ������֣��浽���鵱�У���forѭ��
		for( int i =  mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; ++i ){
			words[i] = getRandomChar() + "";
		}
		
		//����˳��
		for( int i = MyGridView.COUNTS_WORDS-1; i >= 0; i-- ){
			int index = random.nextInt( i+1 );
			//����
			String buf = words[index];
			words[index] = words[i];
			words[i] = buf;
			
		}
		
		return words;
	}
	
	
	//�����������
	private char getRandomChar(){
		String str = "";
		int hightPos;
		int lowPos;
		
		Random random = new Random();
		
		hightPos = (176 + Math.abs( random.nextInt(39) ) );
		lowPos = ( 161 + Math.abs( random.nextInt(39 )));
		
		byte[] b = new byte[2];
		b[0] = Integer.valueOf(hightPos).byteValue();
		b[1] = Integer.valueOf(lowPos).byteValue();
		
		try {
			str = new String( b, "GBK" );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return str.charAt(0);
		
	}
	
	private int checkTheAnswer(){
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			//����пյ�˵���𰸻�������
			if( mBtnSelectWords.get(i).mWordString.length() == 0 ){
				return STATUS_ANSWER_LACK;
			}
		}
		
		//�����������������ȷ��
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			sb.append( mBtnSelectWords.get(i).mWordString );
		}
		
		return (sb.toString().equals(mCurrentSong.getmSongName())) ? 
				STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
	}
	
	/**
	 * ������˸
	 */
	private void sparkTheWords(){
		//��ʱ�����
		TimerTask task = new TimerTask(){

			boolean mChange = false;
			int mSpardTimes = 0;
			
			@Override
			public void run() {
				runOnUiThread( new Runnable(){
					public void run(){
						//��ʾ��˸�Ĵ���
						if( ++mSpardTimes > SPASH_TIMES){
							return;
						}
						
						//ִ����˸�߼���������ʾ��ɫ�Ͱ�ɫ����
						for( int i = 0; i < mBtnSelectWords.size(); ++i ){
							mBtnSelectWords.get(i).mViewButton.setTextColor(mChange ? Color.RED : Color.WHITE);
						}
						
						mChange = !mChange;
						
					}
				});
			}
			
		};
		
		Timer timer = new Timer();
		timer.schedule(task, 1, 150);

	}
	
	/**
	 * �Զ�ѡ��һ����
	 */
	private void tipAnswer(){
		boolean tipWord = false;
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			if( mBtnSelectWords.get(i).mWordString.length() == 0 ){
				//���ݵ�ǰ�𰸿�����ѡ���Ӧ�����ֲ�����
				onWordButtonClick(findIsAnswerWord(i));
				tipWord = true;
				//���ٽ������
				if( handleCoins(-getTipCoins()) ){
					//���������������ʱ����ʾ�Ի���
					return;
				}
				break;
			}
		}
		
		//û���ҵ��������Ĵ�
		if( !tipWord ){
			//��˸������ʾ�û�
			sparkTheWords();
		}
	}
	

	
	/**
	 * ɾ������
	 */
	private void deleteOneWord(){
		//���ٽ��
		if( !handleCoins(-getDeleteWordCoins())){
			//��Ҳ�������ʾ�Ի���
			return;
		}
		
		//�����������Ӧ��WordButton����Ϊ���ɼ�
		setButtonVisiable( findNotAnswerWord(), View.INVISIBLE);
		
	}
	
	/**
	 * �ҵ�һ�����Ǵ𰸵����֣������ǵ�ǰ�ɼ���
	 * @return
	 */
	private WordButton findNotAnswerWord(){
		Random random = new Random();
		WordButton buf = null;
		while( true ){
			int index = random.nextInt(MyGridView.COUNTS_WORDS);
			
			buf = mAllWords.get(index);
			
			if( buf.mIsVisible && !isTheAnswerWord(buf) ){
				return buf;
			}
		}
	}
	 
	/**
	 * �ҵ�һ��������
	 * @param index ��ǰ��Ҫ����𰸿������
	 * @return
	 */
	private WordButton findIsAnswerWord( int index ){
		WordButton buf = null;
		
		for( int i = 0; i < MyGridView.COUNTS_WORDS; ++i ){
			buf  = mAllWords.get(i);
			if( buf.mWordString.equals(""+mCurrentSong.getNameCharacters()[index])){
				return buf;
			}
		}
		
		return null;
	}
	
	/**
	 * �ж�ĳ�������Ƿ�Ϊ��
	 * 
	 * @param word
	 * @return
	 */
	private boolean isTheAnswerWord( WordButton word ){
		boolean result = false;
		for( int i = 0; i < mCurrentSong.getNameLength(); ++i ){
			if( word.mWordString.equals(mCurrentSong.getNameCharacters()[i])){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * ���ӻ����ָ���������
	 * @param data ���Ĵ������ӣ����Ĵ������
	 * @return true	����/���ٳɹ���false��ʧ��
	 */
	private boolean handleCoins( int data ){
		//�жϵ�ǰ�ܵĽ�������Ƿ�ɱ�����
		if( mCurrentCoins + data >= 0 ){	//������Լ���
			mCurrentCoins += data;
			//�������Ͻǵ�coins
			mViewCurrentCoins.setText( mCurrentCoins+"" );
			return true;
		}else{	//��Ҳ���ʱ
			
			return false;
		}
	}
	
	private int getDeleteWordCoins(){
		return this.getResources().getInteger(R.integer.pay_delete_word);
	}
	
	private int getTipCoins(){
		return this.getResources().getInteger(R.integer.pay_tip_answer);
	}
	
	/**
	 * ����ɾ����ѡ�����¼�
	 */
	private void handleDeleteWord(){
		ImageButton button = (ImageButton)findViewById(R.id.btn_delete_word);
		
		button.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				deleteOneWord();
			}
		});
	}
	
	/**
	 * ������ʾ�¼�
	 */
	private void handleTipAnswer(){
		ImageButton button = (ImageButton)findViewById(R.id.btn_tip_answer);
		
		button.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tipAnswer();
				
			}
		});
	}
}
