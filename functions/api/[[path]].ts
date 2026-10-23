interface Env {
  DEVTRACK_API_ORIGIN: string;
}

export const onRequest: PagesFunction<Env> = async ({ request, env }) => {
  if (!env.DEVTRACK_API_ORIGIN) {
    return Response.json({ error: "API origin is not configured." }, { status: 503 });
  }

  let apiOrigin: URL;
  try {
    apiOrigin = new URL(env.DEVTRACK_API_ORIGIN);
  } catch {
    return Response.json({ error: "API origin is invalid." }, { status: 503 });
  }
  if (apiOrigin.protocol !== "https:" && apiOrigin.hostname !== "localhost") {
    return Response.json({ error: "The API origin must use HTTPS." }, { status: 503 });
  }

  const incoming = new URL(request.url);
  const upstream = new URL(`${incoming.pathname}${incoming.search}`, apiOrigin);
  const proxiedRequest = new Request(upstream, request);
  return fetch(proxiedRequest);
};
