/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops.geom.geom3d;

import net.imagej.ops.Contingent;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;

import org.scijava.plugin.Plugin;

/**
 * This {@link Op} computes the 2nd multi variate of a {@link IterableRegion}
 * (Label).
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @param <B> BooleanType
 */
@Plugin(type = Ops.Geometric.SecondMultiVariate.class)
public class DefaultSecondMultiVariate3D<B extends BooleanType<B>> extends
	AbstractUnaryFunctionOp<IterableRegion<B>, DefaultCovarianceOf2ndMultiVariate3D>
	implements Ops.Geometric.SecondMultiVariate, Contingent
{

	private UnaryFunctionOp<IterableRegion<B>, RealLocalizable> centroid;

	@Override
	public void initialize() {
		centroid = Functions.unary(ops(), Ops.Geometric.Centroid.class, RealLocalizable.class, in());
	}

	@Override
	public DefaultCovarianceOf2ndMultiVariate3D compute1(final IterableRegion<B> input) {
		final DefaultCovarianceOf2ndMultiVariate3D output = new DefaultCovarianceOf2ndMultiVariate3D();
		Cursor<Void> c = input.localizingCursor();
		double[] pos = new double[3];
		double[] computedCentroid = new double[3];
		centroid.compute1(input).localize(computedCentroid);
		final double mX = computedCentroid[0];
		final double mY = computedCentroid[1];
		final double mZ = computedCentroid[2];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			output.setS00(output.getS200() + (pos[0] - mX) * (pos[0] - mX));
			output.setS11(output.getS020() + (pos[1] - mX) * (pos[1] - mY));
			output.setS22(output.getS002() + (pos[2] - mX) * (pos[2] - mZ));
			output.setS01(output.getS01() + (pos[0] - mY) * (pos[1] - mY));
			output.setS02(output.getS101() + (pos[0] - mY) * (pos[2] - mZ));
			output.setS12(output.getS011() + (pos[1] - mZ) * (pos[2] - mZ));
		}

		final double size = input.size();
		final double s200 = output.getS200() / size;
		output.setS00(s200);
		final double s020 = output.getS020() / size;
		output.setS11(s020);
		final double s002 = output.getS002() / size;
		output.setS22(s002);
		final double s110 = output.getS01() / size;
		output.setS01(s110);
		final double s101 = output.getS101() / size;
		output.setS02(s101);
		final double s011 = output.getS011() / size;
		output.setS12(s011);

		return output;
	}

	@Override
	public boolean conforms() {
		return in().numDimensions() == 3;
	}

}
