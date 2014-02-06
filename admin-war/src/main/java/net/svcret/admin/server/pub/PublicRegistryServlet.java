package net.svcret.admin.server.pub;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.svcret.admin.api.AdminServiceProvider;
import net.svcret.admin.api.IAdminServiceLocal;
import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.server.rpc.BaseRpcServlet;
import net.svcret.admin.shared.model.ModelUpdateRequest;

public class PublicRegistryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private IAdminServiceLocal myAdminSvc;

	@Override
	public void init() throws ServletException {
		super.init();
		
		myAdminSvc = AdminServiceProvider.getInstance().getAdminService();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		execute(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		execute(request, response);
	}

	private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ModelUpdateRequest req = new ModelUpdateRequest();
		if (BaseRpcServlet.isMockMode()) {
			request.setAttribute("domainList", BaseRpcServlet.getMock().loadModelUpdate(req).getDomainList());
			request.setAttribute("config", BaseRpcServlet.getMock().loadConfig());
		}else {
			try {
				request.setAttribute("domainList", myAdminSvc.loadModelUpdate(req).getDomainList());
				request.setAttribute("config", myAdminSvc.loadConfig());
			} catch (ProcessingException e) {
				throw new ServletException(e);
			} catch (UnexpectedFailureException e) {
				throw new ServletException(e);
			}
		}
		request.getRequestDispatcher("/WEB-INF/jsp/public_registry.jsp").forward(request, response);
	}
	
}
