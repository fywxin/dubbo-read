package com.image;

import com.image.api.ImageParam;
import com.image.api.ImgService;

public class ImgServiceImpl implements ImgService {

	@Override
	public String saveImg(ImageParam param) {
		System.out.println(param);
		return "策划你哥哥";
	}

}
