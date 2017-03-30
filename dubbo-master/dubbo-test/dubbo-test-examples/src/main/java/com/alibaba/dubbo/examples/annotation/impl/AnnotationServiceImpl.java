/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.examples.annotation.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.examples.annotation.api.AnnotationService;

/**
 * AsyncServiceImpl
 * 
 * @author william.liangf
 */
@Service
public class AnnotationServiceImpl implements AnnotationService {

    public String sayHello(String name) {
        System.out.println("async provider received 111111: " + name);
        return "annotation: hello, " + name;
    }

	@Override
	public String $echo(String word) {
		return "服务端："+word;
	}

}