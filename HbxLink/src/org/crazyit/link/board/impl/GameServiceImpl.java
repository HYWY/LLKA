package org.crazyit.link.board.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.crazyit.link.board.AbstractBoard;
import org.crazyit.link.board.GameService;
import org.crazyit.link.object.GameConf;
import org.crazyit.link.object.LinkInfo;
import org.crazyit.link.view.Piece;

import android.graphics.Point;

/**
 * Description: ��Ϸ�߼���ʵ���� <br/>
 * site: <a href="http://www.crazyit.org">crazyit.org</a> <br/>
 * Copyright (C), 2001-2012, Yeeku.H.Lee <br/>
 * This program is protected by copyright laws. <br/>
 * Program Name: <br/>
 * Date:
 * 
 * @author Yeeku.H.Lee kongyeeku@163.com
 * @version 1.0
 */
public class GameServiceImpl implements GameService
{

	private Piece[][] pieces;

	private GameConf config;

	public GameServiceImpl(GameConf config)
	{

		this.config = config;
	}

	@Override
	public void start()
	{

		AbstractBoard board = null;
		Random random = new Random();

		int index = random.nextInt(4);

		switch (index)
		{
			case 0:

				board = new VerticalBoard();
				break;
			case 1:

				board = new HorizontalBoard();
				break;
			default:

				board = new FullBoard();
				break;
		}

		this.pieces = board.create(config);
	}


	@Override
	public Piece[][] getPieces()
	{
		return this.pieces;
	}


	@Override
	public boolean hasPieces()
	{

		for (int i = 0; i < pieces.length; i++)
		{
			for (int j = 0; j < pieces[i].length; j++)
			{

				if (pieces[i][j] != null)
				{
					return true;
				}
			}
		}
		return false;
	}

	
	@Override
	public Piece findPiece(float touchX, float touchY)
	{
		int relativeX = (int) touchX - this.config.getBeginImageX();
		int relativeY = (int) touchY - this.config.getBeginImageY();
		if (relativeX < 0 || relativeY < 0)
		{
			return null;
		}
		int indexX = getIndex(relativeX, GameConf.PIECE_WIDTH);
		int indexY = getIndex(relativeY, GameConf.PIECE_HEIGHT);
		if (indexX < 0 || indexY < 0)
		{
			return null;
		}

		if (indexX >= this.config.getXSize()
			|| indexY >= this.config.getYSize())
		{
			return null;
		}
		return this.pieces[indexX][indexY];
	}

	private int getIndex(int relative, int size)
	{

		int index = -1;

		if (relative % size == 0)
		{
			index = relative / size - 1;
		}
		else
		{

			index = relative / size;
		}
		return index;
	}
	
	@Override
	public LinkInfo link(Piece p1, Piece p2)
	{
		if (p1.equals(p2))
			return null;
		if (!p1.isSameImage(p2))
			return null;
		if (p2.getIndexX() < p1.getIndexX())
			return link(p2, p1);

		Point p1Point = p1.getCenter();

		Point p2Point = p2.getCenter();

		if (p1.getIndexY() == p2.getIndexY())
		{

			if (!isXBlock(p1Point, p2Point, GameConf.PIECE_WIDTH))
			{
				return new LinkInfo(p1Point, p2Point);
			}
		}

		if (p1.getIndexX() == p2.getIndexX())
		{
			if (!isYBlock(p1Point, p2Point, GameConf.PIECE_HEIGHT))
			{

				return new LinkInfo(p1Point, p2Point);
			}
		}

		Point cornerPoint = getCornerPoint(p1Point, p2Point,
			GameConf.PIECE_WIDTH, GameConf.PIECE_HEIGHT);
		if (cornerPoint != null)
		{
			return new LinkInfo(p1Point, cornerPoint, p2Point);
		}

		Map<Point, Point> turns = getLinkPoints(p1Point, p2Point,
			GameConf.PIECE_WIDTH, GameConf.PIECE_WIDTH);
		if (turns.size() != 0)
		{
			return getShortcut(p1Point, p2Point, turns,
				getDistance(p1Point, p2Point));
		}
		return null;
	}

	/**
	 * ��ȡ����ת�۵�����
	 * 
	 * @param point1
	 * @param point2
	 * @return Map�����ÿ��key-value�Դ���һ�����ӷ�ʽ��
	 *   ����key��value�ֱ�����1������2�����ӵ�
	 */
	private Map<Point, Point> getLinkPoints(Point point1, Point point2,
		int pieceWidth, int pieceHeight)
	{
		Map<Point, Point> result = new HashMap<Point, Point>();

		List<Point> p1UpChanel = getUpChanel(point1, point2.y, pieceHeight);
		List<Point> p1RightChanel = getRightChanel(point1, point2.x, pieceWidth);
		List<Point> p1DownChanel = getDownChanel(point1, point2.y, pieceHeight);

		List<Point> p2DownChanel = getDownChanel(point2, point1.y, pieceHeight);
		List<Point> p2LeftChanel = getLeftChanel(point2, point1.x, pieceWidth);
		List<Point> p2UpChanel = getUpChanel(point2, point1.y, pieceHeight);

		int heightMax = (this.config.getYSize() + 1) * pieceHeight
			+ this.config.getBeginImageY();

		int widthMax = (this.config.getXSize() + 1) * pieceWidth
			+ this.config.getBeginImageX();


		if (isLeftUp(point1, point2) || isLeftDown(point1, point2))
		{

			return getLinkPoints(point2, point1, pieceWidth, pieceHeight);
		}

		if (point1.y == point2.y)
		{

			p1UpChanel = getUpChanel(point1, 0, pieceHeight);

			p2UpChanel = getUpChanel(point2, 0, pieceHeight);
			Map<Point, Point> upLinkPoints = getXLinkPoints(p1UpChanel,
				p2UpChanel, pieceHeight);

			p1DownChanel = getDownChanel(point1, heightMax, pieceHeight);

			p2DownChanel = getDownChanel(point2, heightMax, pieceHeight);
			Map<Point, Point> downLinkPoints = getXLinkPoints(p1DownChanel,
				p2DownChanel, pieceHeight);
			result.putAll(upLinkPoints);
			result.putAll(downLinkPoints);
		}

		if (point1.x == point2.x)
		{

			List<Point> p1LeftChanel = getLeftChanel(point1, 0, pieceWidth);

			p2LeftChanel = getLeftChanel(point2, 0, pieceWidth);
			Map<Point, Point> leftLinkPoints = getYLinkPoints(p1LeftChanel,
				p2LeftChanel, pieceWidth);

			p1RightChanel = getRightChanel(point1, widthMax, pieceWidth);

			List<Point> p2RightChanel = getRightChanel(point2, widthMax,
				pieceWidth);
			Map<Point, Point> rightLinkPoints = getYLinkPoints(p1RightChanel,
				p2RightChanel, pieceWidth);
			result.putAll(leftLinkPoints);
			result.putAll(rightLinkPoints);
		}

		if (isRightUp(point1, point2))
		{		

			Map<Point, Point> upDownLinkPoints = getXLinkPoints(p1UpChanel,
				p2DownChanel, pieceWidth);

			Map<Point, Point> rightLeftLinkPoints = getYLinkPoints(
				p1RightChanel, p2LeftChanel, pieceHeight);

			p1UpChanel = getUpChanel(point1, 0, pieceHeight);

			p2UpChanel = getUpChanel(point2, 0, pieceHeight);

			Map<Point, Point> upUpLinkPoints = getXLinkPoints(p1UpChanel,
				p2UpChanel, pieceWidth);

			p1DownChanel = getDownChanel(point1, heightMax, pieceHeight);

			p2DownChanel = getDownChanel(point2, heightMax, pieceHeight);

			Map<Point, Point> downDownLinkPoints = getXLinkPoints(p1DownChanel,
				p2DownChanel, pieceWidth);

			p1RightChanel = getRightChanel(point1, widthMax, pieceWidth);

			List<Point> p2RightChanel = getRightChanel(point2, widthMax,
				pieceWidth);

			Map<Point, Point> rightRightLinkPoints = getYLinkPoints(
				p1RightChanel, p2RightChanel, pieceHeight);

			List<Point> p1LeftChanel = getLeftChanel(point1, 0, pieceWidth);

			p2LeftChanel = getLeftChanel(point2, 0, pieceWidth);

			Map<Point, Point> leftLeftLinkPoints = getYLinkPoints(p1LeftChanel,
				p2LeftChanel, pieceHeight);
			result.putAll(upDownLinkPoints);
			result.putAll(rightLeftLinkPoints);
			result.putAll(upUpLinkPoints);
			result.putAll(downDownLinkPoints);
			result.putAll(rightRightLinkPoints);
			result.putAll(leftLeftLinkPoints);
		}

		if (isRightDown(point1, point2))
		{

			Map<Point, Point> downUpLinkPoints = getXLinkPoints(p1DownChanel,
				p2UpChanel, pieceWidth);

			Map<Point, Point> rightLeftLinkPoints = getYLinkPoints(
				p1RightChanel, p2LeftChanel, pieceHeight);

			p1UpChanel = getUpChanel(point1, 0, pieceHeight);

			p2UpChanel = getUpChanel(point2, 0, pieceHeight);

			Map<Point, Point> upUpLinkPoints = getXLinkPoints(p1UpChanel,
				p2UpChanel, pieceWidth);

			p1DownChanel = getDownChanel(point1, heightMax, pieceHeight);

			p2DownChanel = getDownChanel(point2, heightMax, pieceHeight);

			Map<Point, Point> downDownLinkPoints = getXLinkPoints(p1DownChanel,
				p2DownChanel, pieceWidth);

			List<Point> p1LeftChanel = getLeftChanel(point1, 0, pieceWidth);

			p2LeftChanel = getLeftChanel(point2, 0, pieceWidth);

			Map<Point, Point> leftLeftLinkPoints = getYLinkPoints(p1LeftChanel,
				p2LeftChanel, pieceHeight);

			p1RightChanel = getRightChanel(point1, widthMax, pieceWidth);

			List<Point> p2RightChanel = getRightChanel(point2, widthMax,
				pieceWidth);

			Map<Point, Point> rightRightLinkPoints = getYLinkPoints(
				p1RightChanel, p2RightChanel, pieceHeight);
			result.putAll(downUpLinkPoints);
			result.putAll(rightLeftLinkPoints);
			result.putAll(upUpLinkPoints);
			result.putAll(downDownLinkPoints);
			result.putAll(leftLeftLinkPoints);
			result.putAll(rightRightLinkPoints);
		}
		return result;
	}
	
	/**
	 * ��ȡp1��p2֮����̵�������Ϣ
	 * 
	 * @param p1
	 * @param p2
	 * @param turns ��ת�۵��map
	 * @param shortDistance ����֮�����̾���
	 * @return p1��p2֮����̵�������Ϣ
	 */
	private LinkInfo getShortcut(Point p1, Point p2, Map<Point, Point> turns,
		int shortDistance)
	{
		List<LinkInfo> infos = new ArrayList<LinkInfo>();

		for (Point point1 : turns.keySet())
		{
			Point point2 = turns.get(point1);

			infos.add(new LinkInfo(p1, point1, point2, p2));
		}
		return getShortcut(infos, shortDistance);
	}
	
	/**
	 * ��infos�л�ȡ��������̵��Ǹ�LinkInfo����
	 * 
	 * @param infos
	 * @return ��������̵��Ǹ�LinkInfo����
	 */
	private LinkInfo getShortcut(List<LinkInfo> infos, int shortDistance)
	{
		int temp1 = 0;
		LinkInfo result = null;
		for (int i = 0; i < infos.size(); i++)
		{
			LinkInfo info = infos.get(i);

			int distance = countAll(info.getLinkPoints());

			if (i == 0)
			{
				temp1 = distance - shortDistance;
				result = info;
			}

			if (distance - shortDistance < temp1)
			{
				temp1 = distance - shortDistance;
				result = info;
			}
		}
		return result;
	}
	
	/**
	 * ����List<Point>�����е�ľ����ܺ�
	 * 
	 * @param points ��Ҫ��������ӵ�
	 * @return ���е�ľ�����ܺ�
	 */
	private int countAll(List<Point> points)
	{
		int result = 0;
		for (int i = 0; i < points.size() - 1; i++)
		{

			Point point1 = points.get(i);

			Point point2 = points.get(i + 1);

			result += getDistance(point1, point2);
		}
		return result;
	}
	
	/**
	 * ��ȡ����LinkPoint֮�����̾���
	 * 
	 * @param p1 ��һ����
	 * @param p2 �ڶ�����
	 * @return ������ľ�������ܺ�
	 */
	private int getDistance(Point p1, Point p2)
	{
		int xDistance = Math.abs(p1.x - p2.x);
		int yDistance = Math.abs(p1.y - p2.y);
		return xDistance + yDistance;
	}
	
	/**
	 * ������������, ���жϵ�һ�����ϵ�Ԫ�ص�x��������һ�������е�Ԫ��x������ͬ(����), 
	 * �����ͬ, ����ͬһ��, ���ж��Ƿ����ϰ�, û����ӵ������Map��ȥ
	 * 
	 * @param p1Chanel
	 * @param p2Chanel
	 * @param pieceHeight
	 * @return
	 */
	private Map<Point, Point> getYLinkPoints(List<Point> p1Chanel,
		List<Point> p2Chanel, int pieceHeight)
	{
		Map<Point, Point> result = new HashMap<Point, Point>();
		for (int i = 0; i < p1Chanel.size(); i++)
		{
			Point temp1 = p1Chanel.get(i);
			for (int j = 0; j < p2Chanel.size(); j++)
			{
				Point temp2 = p2Chanel.get(j);

				if (temp1.x == temp2.x)
				{

					if (!isYBlock(temp1, temp2, pieceHeight))
					{
						result.put(temp1, temp2);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * ������������, ���жϵ�һ�����ϵ�Ԫ�ص�y��������һ�������е�Ԫ��y������ͬ(����),
	 * �����ͬ, ����ͬһ��, ���ж��Ƿ����ϰ�, û�� ��ӵ������map��ȥ
	 * 
	 * @param p1Chanel
	 * @param p2Chanel
	 * @param pieceWidth
	 * @return ��ſ��Ժ���ֱ�����ӵ����ӵ�ļ�ֵ��
	 */
	private Map<Point, Point> getXLinkPoints(List<Point> p1Chanel,
		List<Point> p2Chanel, int pieceWidth)
	{
		Map<Point, Point> result = new HashMap<Point, Point>();
		for (int i = 0; i < p1Chanel.size(); i++)
		{

			Point temp1 = p1Chanel.get(i);

			for (int j = 0; j < p2Chanel.size(); j++)
			{
				Point temp2 = p2Chanel.get(j);

				if (temp1.y == temp2.y)
				{
					if (!isXBlock(temp1, temp2, pieceWidth))
					{

						result.put(temp1, temp2);
					}
				}
			}
		}
		return result;
	}

	/**
	 * �ж�point2�Ƿ���point1�����Ͻ�
	 * 
	 * @param point1
	 * @param point2
	 * @return p2λ��p1�����Ͻ�ʱ����true�����򷵻�false
	 */
	private boolean isLeftUp(Point point1, Point point2)
	{
		return (point2.x < point1.x && point2.y < point1.y);
	}
	
	/**
	 * �ж�point2�Ƿ���point1�����½�
	 * 
	 * @param point1
	 * @param point2
	 * @return p2λ��p1�����½�ʱ����true�����򷵻�false
	 */
	private boolean isLeftDown(Point point1, Point point2)
	{
		return (point2.x < point1.x && point2.y > point1.y);
	}
	
	/**
	 * �ж�point2�Ƿ���point1�����Ͻ�
	 * 
	 * @param point1
	 * @param point2
	 * @return p2λ��p1�����Ͻ�ʱ����true�����򷵻�false
	 */
	private boolean isRightUp(Point point1, Point point2)
	{
		return (point2.x > point1.x && point2.y < point1.y);
	}
	
	/**
	 * �ж�point2�Ƿ���point1�����½�
	 * 
	 * @param point1
	 * @param point2
	 * @return p2λ��p1�����½�ʱ����true�����򷵻�false
	 */
	private boolean isRightDown(Point point1, Point point2)
	{
		return (point2.x > point1.x && point2.y > point1.y);
	}

	/**
	 * ��ȡ��������ͬһ�л���ͬһ�е�������ֱ�����ӵ�, ��ֻ��һ��ת�۵�
	 * 
	 * @param point1 ��һ����
	 * @param point2 �ڶ�����
	 * @return ��������ͬһ�л���ͬһ�е�������ֱ�����ӵ�
	 */
	private Point getCornerPoint(Point point1, Point point2, int pieceWidth,
		int pieceHeight)
	{

		if (isLeftUp(point1, point2) || isLeftDown(point1, point2))
		{

			return getCornerPoint(point2, point1, pieceWidth, pieceHeight);
		}

		List<Point> point1RightChanel = getRightChanel(point1, point2.x,
			pieceWidth);
		List<Point> point1UpChanel = getUpChanel(point1, point2.y, pieceHeight);
		List<Point> point1DownChanel = getDownChanel(point1, point2.y,
			pieceHeight);

		List<Point> point2DownChanel = getDownChanel(point2, point1.y,
			pieceHeight);
		List<Point> point2LeftChanel = getLeftChanel(point2, point1.x,
			pieceWidth);
		List<Point> point2UpChanel = getUpChanel(point2, point1.y, pieceHeight);
		if (isRightUp(point1, point2))
		{

			Point linkPoint1 = getWrapPoint(point1RightChanel, point2DownChanel);

			Point linkPoint2 = getWrapPoint(point1UpChanel, point2LeftChanel);

			return (linkPoint1 == null) ? linkPoint2 : linkPoint1;
		}
		if (isRightDown(point1, point2))
		{

			Point linkPoint1 = getWrapPoint(point1DownChanel, point2LeftChanel);

			Point linkPoint2 = getWrapPoint(point1RightChanel, point2UpChanel);
			return (linkPoint1 == null) ? linkPoint2 : linkPoint1;
		}
		return null;
	}

	/**
	 * ��������ͨ��, ��ȡ���ǵĽ���
	 * 
	 * @param p1Chanel ��һ�����ͨ��
	 * @param p2Chanel �ڶ������ͨ��
	 * @return ����ͨ���н��㣬���ؽ��㣬���򷵻�null
	 */
	private Point getWrapPoint(List<Point> p1Chanel, List<Point> p2Chanel)
	{
		for (int i = 0; i < p1Chanel.size(); i++)
		{
			Point temp1 = p1Chanel.get(i);
			for (int j = 0; j < p2Chanel.size(); j++)
			{
				Point temp2 = p2Chanel.get(j);
				if (temp1.equals(temp2))
				{

					return temp1;
				}
			}
		}
		return null;
	}

	/**
	 * �ж�����y������ͬ�ĵ����֮���Ƿ����ϰ�, ��p1Ϊ�������ұ���
	 * 
	 * @param p1
	 * @param p2
	 * @param pieceWidth
	 * @return ����Piece֮�����ϰ�����true�����򷵻�false
	 */
	private boolean isXBlock(Point p1, Point p2, int pieceWidth)
	{
		if (p2.x < p1.x)
		{

			return isXBlock(p2, p1, pieceWidth);
		}
		for (int i = p1.x + pieceWidth; i < p2.x; i = i + pieceWidth)
		{
			if (hasPiece(i, p1.y))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * �ж�����x������ͬ�ĵ����֮���Ƿ����ϰ�, ��p1Ϊ�������±���
	 * 
	 * @param p1
	 * @param p2
	 * @param pieceHeight
	 * @return ����Piece֮�����ϰ�����true�����򷵻�false
	 */
	private boolean isYBlock(Point p1, Point p2, int pieceHeight)
	{
		if (p2.y < p1.y)
		{

			return isYBlock(p2, p1, pieceHeight);
		}
		for (int i = p1.y + pieceHeight; i < p2.y; i = i + pieceHeight)
		{
			if (hasPiece(p1.x, i))
			{
	
				return true;
			}
		}
		return false;
	}

	/**
	 * �ж�GamePanel�е�x, y�������Ƿ���Piece����
	 * 
	 * @param x
	 * @param y
	 * @return true ��ʾ�и�������piece���� false ��ʾû��
	 */
	private boolean hasPiece(int x, int y)
	{
		if (findPiece(x, y) == null)
			return false;
		return true;
	}

	/**
	 * ��һ��Point����,�����������ͨ��
	 * 
	 * @param p
	 * @param pieceWidth pieceͼƬ�Ŀ�
	 * @param min �������ʱ��С�Ľ���
	 * @return ����Point��ߵ�ͨ��
	 */
	private List<Point> getLeftChanel(Point p, int min, int pieceWidth)
	{
		List<Point> result = new ArrayList<Point>();

		for (int i = p.x - pieceWidth; i >= min
			; i = i - pieceWidth)
		{

			if (hasPiece(i, p.y))
			{
				return result;
			}
			result.add(new Point(i, p.y));
		}
		return result;
	}
	
	/**
	 * ��һ��Point����, ���������ұ�ͨ��
	 * 
	 * @param p
	 * @param pieceWidth
	 * @param max ����ʱ�����ҽ���
	 * @return ����Point�ұߵ�ͨ��
	 */
	private List<Point> getRightChanel(Point p, int max, int pieceWidth)
	{
		List<Point> result = new ArrayList<Point>();
	
		for (int i = p.x + pieceWidth; i <= max
			; i = i + pieceWidth)
		{
	
			if (hasPiece(i, p.y))
			{
				return result;
			}
			result.add(new Point(i, p.y));
		}
		return result;
	}
	
	/**
	 * ��һ��Point����, ������������ͨ��
	 * 
	 * @param p
	 * @param min ���ϱ���ʱ��С�Ľ���
	 * @param pieceHeight
	 * @return ����Point�����ͨ��
	 */
	private List<Point> getUpChanel(Point p, int min, int pieceHeight)
	{
		List<Point> result = new ArrayList<Point>();

		for (int i = p.y - pieceHeight; i >= min
			; i = i - pieceHeight)
		{
	
			if (hasPiece(p.x, i))
			{
	
				return result;
			}
			result.add(new Point(p.x, i));
		}
		return result;
	}
	
	/**
	 * ��һ��Point����, ������������ͨ��
	 * 
	 * @param p
	 * @param max ���ϱ���ʱ��������
	 * @return ����Point�����ͨ��
	 */
	private List<Point> getDownChanel(Point p, int max, int pieceHeight)
	{
		List<Point> result = new ArrayList<Point>();
	
		for (int i = p.y + pieceHeight; i <= max
			; i = i + pieceHeight)
		{
	
			if (hasPiece(p.x, i))
			{
	
				return result;
			}
			result.add(new Point(p.x, i));
		}
		return result;
	}
}
