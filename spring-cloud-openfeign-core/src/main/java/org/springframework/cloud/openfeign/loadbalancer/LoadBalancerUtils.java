/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.openfeign.loadbalancer;

import java.io.IOException;
import java.util.Set;

import feign.Client;
import feign.Request;
import feign.Response;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;

/**
 * @author Olga Maciaszek-Sharma
 *
 * A utility class for handling {@link LoadBalancerLifecycle} calls.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
final class LoadBalancerUtils {

	private LoadBalancerUtils() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
			Request feignRequest, org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
			Set<LoadBalancerLifecycle> supportedLifecycleProcessors, boolean loadBalanced) throws IOException {
		try {
			Response response = feignClient.execute(feignRequest, options);
			if (loadBalanced) {
				supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
						.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS, lbResponse, response)));
			}
			return response;
		}
		catch (Exception exception) {
			if (loadBalanced) {
				supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
						.onComplete(new CompletionContext<>(CompletionContext.Status.FAILED, exception, lbResponse)));
			}
			throw exception;
		}
	}

	static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
			Request feignRequest, org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
			Set<LoadBalancerLifecycle> supportedLifecycleProcessors) throws IOException {
		return executeWithLoadBalancerLifecycleProcessing(feignClient, options, feignRequest, lbResponse,
				supportedLifecycleProcessors, true);
	}

}
