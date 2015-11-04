package org.crazyit.link.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.crazyit.link.R;
import org.crazyit.link.view.PieceImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Description: 图片资源工具类, 主要用于读取游戏图片资源值
 * <br/>site: <a href="http://www.crazyit.org">crazyit.org</a> 
 * <br/>Copyright (C), 2001-2012, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:Download by http://www.codefans.net
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class ImageUtil
{

	private static List<Integer> imageValues = getImageValues();

	
	public static List<Integer> getImageValues()
	{
		try
		{
	
			Field[] drawableFields = R.drawable.class.getFields();
			List<Integer> resourceValues = new ArrayList<Integer>();
			for (Field field : drawableFields)
			{
	
				if (field.getName().indexOf("p_") != -1)
				{
					resourceValues.add(field.getInt(R.drawable.class));
				}
			}
			return resourceValues;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * 随机从sourceValues的集合中获取size个图片ID, 返回结果为图片ID的集合
	 * 
	 * @param sourceValues 从中获取的集合
	 * @param size 需要获取的个数
	 * @return size个图片ID的集合
	 */
	public static List<Integer> getRandomValues(List<Integer> sourceValues,
		int size)
	{
	
		Random random = new Random();

		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < size; i++)
		{
			try
			{
	
				int index = random.nextInt(sourceValues.size());
	
				Integer image = sourceValues.get(index);

				result.add(image);
			}
			catch (IndexOutOfBoundsException e)
			{
				return result;
			}
		}
		return result;
	}

	/**
	 * 从drawable目录中中获取size个图片资源ID(以p_为前缀的资源名称), 其中size为游戏数量
	 * 
	 * @param size 需要获取的图片ID的数量
	 * @return size个图片ID的集合
	 */
	public static List<Integer> getPlayValues(int size)
	{
		if (size % 2 != 0)
		{

			size += 1;
		}

		List<Integer> playImageValues = getRandomValues(imageValues, size / 2);
	
		playImageValues.addAll(playImageValues);
	
		Collections.shuffle(playImageValues);
		return playImageValues;
	}

	/**
	 * 将图片ID集合转换PieceImage对象集合，PieceImage封装了图片ID与图片本身
	 * 
	 * @param context
	 * @param resourceValues
	 * @return size个PieceImage对象的集合
	 */
	public static List<PieceImage> getPlayImages(Context context, int size)
	{

		List<Integer> resourceValues = getPlayValues(size);
		List<PieceImage> result = new ArrayList<PieceImage>();

		for (Integer value : resourceValues)
		{
	
			Bitmap bm = BitmapFactory.decodeResource(
				context.getResources(),  value);

			PieceImage pieceImage = new PieceImage(bm, value);
			result.add(pieceImage);
		}
		return result;
	}


	public static Bitmap getSelectImage(Context context)
	{
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
			R.drawable.selected);
		return bm;
	}
}
