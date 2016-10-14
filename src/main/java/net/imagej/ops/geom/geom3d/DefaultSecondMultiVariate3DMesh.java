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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import net.imagej.ops.Contingent;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.geom.geom3d.mesh.DefaultMesh;
import net.imagej.ops.geom.geom3d.mesh.Facet;
import net.imagej.ops.geom.geom3d.mesh.Mesh;
import net.imagej.ops.geom.geom3d.mesh.TriangularFacet;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.scijava.plugin.Plugin;

/**
 * This {@link Op} computes the 2nd multi variate of a {@link IterableRegion}
 * (Label).
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @param <B>
 *            BooleanType
 */
@Plugin(type = Ops.Geometric.SecondMultiVariate.class)
public class DefaultSecondMultiVariate3DMesh extends AbstractUnaryFunctionOp<Mesh, DefaultCovarianceOf2ndMultiVariate3D>
		implements Ops.Geometric.SecondMultiVariate, Contingent {

	private UnaryFunctionOp<Mesh, RealLocalizable> centroid;

	@Override
	public void initialize() {
		centroid = Functions.unary(ops(), Ops.Geometric.Centroid.class, RealLocalizable.class, in());
	}

	@Override
	public DefaultCovarianceOf2ndMultiVariate3D compute1(final Mesh input) {
		final DefaultCovarianceOf2ndMultiVariate3D output = new DefaultCovarianceOf2ndMultiVariate3D();
		final Iterator<Facet> c = input.getFacets().iterator();
		double[] computedCentroid = new double[3];
		centroid.compute1(input).localize(computedCentroid);
		Vector3D o = new Vector3D(computedCentroid);

		BlockRealMatrix tensor = new BlockRealMatrix(3, 3);
		while (c.hasNext()) {
			final TriangularFacet tf = (TriangularFacet) c.next();
			Vector3D p1 = new Vector3D(tf.getVertex(0).getX(), tf.getVertex(0).getY(), tf.getVertex(0).getZ());
			Vector3D p2 = new Vector3D(tf.getVertex(1).getX(), tf.getVertex(1).getY(), tf.getVertex(1).getZ());
			Vector3D p3 = new Vector3D(tf.getVertex(2).getX(), tf.getVertex(2).getY(), tf.getVertex(2).getZ());
			
			tensor = tensor.add(tetrahedronInertiaTensor(tf.getVertex(0), tf.getVertex(1), tf.getVertex(2), o, o));
//					.scalarMultiply(tetrahedronVolume(p1,p2,p3,o)));
		}

		EigenDecomposition ed = new EigenDecomposition(tensor);
		System.out.println(o.getX() + ", " + o.getY() + ", " + o.getZ() + ", " + ed.getRealEigenvalue(0)
				+ ", " + ed.getRealEigenvalue(1) + ", " + ed.getRealEigenvalue(2));

		output.setS00(tensor.getEntry(0, 0));
		output.setS01(tensor.getEntry(0, 1));
		output.setS02(tensor.getEntry(0, 2));
		output.setS11(tensor.getEntry(1, 1));
		output.setS12(tensor.getEntry(1, 2));
		output.setS22(tensor.getEntry(2, 2));

		return output;
	}

	private BlockRealMatrix tetrahedronInertiaTensor(final RealLocalizable p1, final RealLocalizable p2,
			final RealLocalizable p3, final Vector3D p4, final Vector3D o) {
		final double oX = o.getX();
		final double oY = o.getY();
		final double oZ = o.getZ();

		final double x1 = p1.getDoublePosition(0) - oX;
		final double y1 = p1.getDoublePosition(1) - oY;
		final double z1 = p1.getDoublePosition(2) - oZ;

		final double x2 = p2.getDoublePosition(0) - oX;
		final double y2 = p2.getDoublePosition(1) - oY;
		final double z2 = p2.getDoublePosition(2) - oZ;

		final double x3 = p3.getDoublePosition(0) - oX;
		final double y3 = p3.getDoublePosition(1) - oY;
		final double z3 = p3.getDoublePosition(2) - oZ;

		final double x4 = p4.getX() - oX;
		final double y4 = p4.getY() - oY;
		final double z4 = p4.getZ() - oZ;

		final double volume = tetrahedronVolume(new Vector3D(x1,y1,z1), new Vector3D(x2,y2,z2), new Vector3D(x3,y3,z3), p4);

		final double mu = 1;

		final double a = mu * 6 * volume
				* (y1 * y1 + y1 * y2 + y2 * y2 + y1 * y3 + y2 * y3 + y3 * y3 + y1 * y4 + y2 * y4 + y3 * y4 + y4 * y4
						+ z1 * z1 + z1 * z2 + z2 * z2 + z1 * z3 + z2 * z3 + z3 * z3 + z1 * z4 + z2 * z4 + z3 * z4
						+ z4 * z4)
				/ 60.0;
		final double b = mu * 6 * volume
				* (x1 * x1 + x1 * x2 + x2 * x2 + x1 * x3 + x2 * x3 + x3 * x3 + x1 * x4 + x2 * x4 + x3 * x4 + x4 * x4
						+ z1 * z1 + z1 * z2 + z2 * z2 + z1 * z3 + z2 * z3 + z3 * z3 + z1 * z4 + z2 * z4 + z3 * z4
						+ z4 * z4)
				/ 60.0;
		final double c = mu * 6 * volume
				* (x1 * x1 + x1 * x2 + x2 * x2 + x1 * x3 + x2 * x3 + x3 * x3 + x1 * x4 + x2 * x4 + x3 * x4 + x4 * x4
						+ y1 * y1 + y1 * y2 + y2 * y2 + y1 * y3 + y2 * y3 + y3 * y3 + y1 * y4 + y2 * y4 + y3 * y4
						+ y4 * y4)
				/ 60.0;
		final double aa = mu * 6
				* volume * (2 * y1 * z1 + y2 * z1 + y3 * z1 + y4 * z1 + y1 * z2 + 2 * y2 * z2 + y3 * z2 + y4 * z2
						+ y1 * z3 + y2 * z3 + 2 * y3 * z3 + y4 * z3 + y1 * z4 + y2 * z4 + y3 * z4 + 2 * y4 * z4)
				/ 120.0;

		final double bb = mu * 6
				* volume * (2 * x1 * z1 + x2 * z1 + x3 * z1 + x4 * z1 + x1 * z2 + 2 * x2 * z2 + x3 * z2 + x4 * z2
						+ x1 * z3 + x2 * z3 + 2 * x3 * z3 + x4 * z3 + x1 * z4 + x2 * z4 + x3 * z4 + 2 * x4 * z4)
				/ 120.0;

		final double cc = mu * 6
				* volume * (2 * x1 * y1 + x2 * y1 + x3 * y1 + x4 * y1 + x1 * y2 + 2 * x2 * y2 + x3 * y2 + x4 * y2
						+ x1 * y3 + x2 * y3 + 2 * x3 * y3 + x4 * y3 + x1 * y4 + x2 * y4 + x3 * y4 + 2 * x4 * y4)
				/ 120.0;

		final BlockRealMatrix t = new BlockRealMatrix(3, 3);
		t.setRow(0, new double[] { a, -bb, -cc });
		t.setRow(1, new double[] { -bb, b, -aa });
		t.setRow(2, new double[] { -cc, -aa, c });

		return t;
	}

	private double tetrahedronVolume(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
		// https://en.wikipedia.org/wiki/Tetrahedron#Volume
//		final Vector3D a = new Vector3D(new double[] { x1, y1, z1 });
//		final Vector3D b = new Vector3D(new double[] { x2, y2, z2 });
//		final Vector3D c = new Vector3D(new double[] { x3, y3, z3 });
//		final Vector3D d = new Vector3D(x4, y4, z4);
		final double volume = Math.abs(a.subtract(d).dotProduct(b.subtract(d).crossProduct(c.subtract(d)))) / 6.0;
		return volume;
	}

	@Override
	public boolean conforms() {
		return in() != null;
	}

}
