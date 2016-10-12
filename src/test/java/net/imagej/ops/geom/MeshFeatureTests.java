package net.imagej.ops.geom;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.imagej.ops.features.AbstractFeatureTest;
import net.imagej.ops.geom.geom3d.DefaultMarchingCubes;
import net.imagej.ops.geom.geom3d.mesh.DefaultMesh;
import net.imagej.ops.geom.geom3d.mesh.Facet;
import net.imagej.ops.geom.geom3d.mesh.Mesh;
import net.imagej.ops.geom.geom3d.mesh.TriangularFacet;
import net.imagej.ops.geom.geom3d.mesh.Vertex;
import net.imglib2.roi.labeling.LabelRegion;

import org.junit.BeforeClass;
import org.junit.Test;

public class MeshFeatureTests extends AbstractFeatureTest {
	private static final double EPSILON = 10e-12;
	private static LabelRegion<String> ROI;
	private static Mesh mesh;

	@BeforeClass
	public static void setupBefore() {
		ROI = createLabelRegion(getTestImage3D(), 1, 255);
		mesh = getMesh();
	}
	
	@Test
	public void boundarySizeConvexHullMesh() {
		
	}
	
	@Test
	public void boxivityMesh() {
		
	}
	
	@Test 
	public void compactness() {
		
	}
	
	@Test
	public void convexHull3D() {
		
	}
	
	@Test
	public void convexityMesh() {
		
	}
	
	@Test
	public void covarianceOf2ndMultiVariate3D() {
		
	}
	
	@Test
	public void mainElongation() {
		
	}
	
	@Test
	public void marchingCubes() {
		final DefaultMesh result = (DefaultMesh) ops.run(DefaultMarchingCubes.class, ROI);
		final List<Facet> expectedFacets = mesh.getFacets();
		final List<Facet> resultFacets = result.getFacets();
		for (int i = 0; i < expectedFacets.size(); i++) {
			final TriangularFacet tmpR = (TriangularFacet) resultFacets.get(i);
			final TriangularFacet tmpE = (TriangularFacet) expectedFacets.get(i);

			for (int j = 0; j < 3; j++) {
				final Vertex expectedVertex = tmpE.getVertex(j);
				final Vertex resultVertex = tmpR.getVertex(j);
				assertEquals("Triangular Facet point " + j + " differes in x- coordinate:",
						expectedVertex.getDoublePosition(0), resultVertex.getDoublePosition(0), EPSILON);
				assertEquals("Triangular Facet point " + j + " differes in y- coordinate:",
						expectedVertex.getDoublePosition(1), resultVertex.getDoublePosition(1), EPSILON);
				assertEquals("Triangular Facet point " + j + " differes in z- coordinate:",
						expectedVertex.getDoublePosition(2), resultVertex.getDoublePosition(2), EPSILON);
			}
		}
	}
	
	@Test
	public void medianElongation() {
		
	}
	
	@Test
	public void secondMultiVariate3D() {
		
	}
	
	@Test
	public void spareness() {
		
	}
	
	@Test
	public void sphericity() {
		
	}
	
	@Test
	public void surfaceArea() {
		
	}
	
	@Test
	public void voxelization3D() {
		
	}
	
	@Test
	public void rugosityMesh() {
		
	}
	
	@Test
	public void sizeConvexHullMesh() {
		
	}
	
	@Test
	public void solidityMesh() {
		
	}
	
	@Test
	public void verticesCountConvexHullMesh() {
		
	}
	
	@Test
	public void verticesCountMesh() {
		
	}
}
