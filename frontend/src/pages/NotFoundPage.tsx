import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-background items-center justify-center p-8">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold tracking-tight">
          Page not found
        </h1>
        <p className="text-sm text-muted-foreground">
          <Link
            to="/dashboard"
            className="text-primary underline-offset-4 hover:underline"
          >
            Back to dashboard
          </Link>
        </p>
      </div>
    </div>
  );
}
